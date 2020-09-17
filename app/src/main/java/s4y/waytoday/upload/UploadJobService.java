package s4y.waytoday.upload;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.JobIntentService;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.reactivex.subjects.PublishSubject;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.R;
import s4y.waytoday.WTApplication;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.grpc.GRPCChannelProvider;
import s4y.waytoday.grpc.Keys;
import s4y.waytoday.grpc.LocationOuterClass;
import s4y.waytoday.grpc.TrackerGrpc;
import s4y.waytoday.grpc.TrackerOuterClass;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.utils.Bear;
import s4y.waytoday.wsse.Wsse;

import static java.util.UUID.randomUUID;
import static s4y.waytoday.utils.FConv.i;

public class UploadJobService extends JobIntentService {
    private static final String LT = UploadJobService.class.getSimpleName();
    public static final PublishSubject<Status> subjectStatus = PublishSubject.create();
    private static Boolean sIsUploading = false;
    private static Boolean sIsError = false;
    private static final Deque<Location> uploadQueue = new LinkedBlockingDeque<>();
    private final static int MAX_LOCATIONS_MEMORY = 500;
    private final static int PACK_SIZE = 16;
    @Inject
    public GRPCChannelProvider grpcChannelProvider;
    protected ManagedChannel ch = null;
    @Inject
    PreferenceTrackID mTrackID;
    @Inject
    PreferenceSound mSound;

    public static Status uploadStatus() {
        if (sIsError)
            return Status.ERROR;
        if (sIsUploading)
            return Status.UPLOADING;
        int size;
        size = uploadQueue.size();
        if (size > 0)
            return Status.QUEUED;
        return Status.EMPTY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((WTApplication) getApplication()).getAppComponent().inject(this);
    }

    @VisibleForTesting()
    public void destoryGrpcChannel() {
        if (ch != null) {
            try {
                ch.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ch = null;
        }
    }

    @Override
    public void onDestroy() {
        destoryGrpcChannel();
        super.onDestroy();
    }

    private static boolean sPrevIsError;
    private static boolean sPrevIsUploading;
    private static int sPrevSize;

    private void uploadStore() {
        // TODO: do not have store yet
    }

    private void saveQueueToStore() {
        /*
        TODO: while there's no persist store just remove
        the oldest locations from the queue
        */
        while (uploadQueue.size() > MAX_LOCATIONS_MEMORY) {
            uploadQueue.pollFirst();
        }
    }

    public enum Status {EMPTY, QUEUED, UPLOADING, ERROR}

    private synchronized boolean uploadQueue() {
        List<Location> pack = new ArrayList<>();
        boolean completed = false;
        for (; ; ) {
            pack.clear();
            int packSize;
            packSize = Math.min(uploadQueue.size(), PACK_SIZE);
            for (int i = 0; i < packSize; i++) {
                Location head = uploadQueue.peekFirst();
                if (head != null) {
                    pack.add(head);
                }
            }
            if (pack.size() > 0) {
                TrackerOuterClass.AddLocationsRequest.Builder req =
                        TrackerOuterClass.AddLocationsRequest
                                .newBuilder();
                req.setTid(mTrackID.get());
                for (Location location : pack) {
                    req.addLocations(marshall(location));
                }
                if (ch == null) ch = grpcChannelProvider.channel();
                TrackerGrpc.TrackerBlockingStub grpcStub = getGrpcStub();
                try {
                    Metadata headers = new Metadata();
                    headers.put(Keys.wsseKey, Wsse.getToken());

                    grpcStub = MetadataUtils.attachHeaders(grpcStub, headers);
                    TrackerOuterClass.AddLocationResponse resp = grpcStub.addLocations(req.build());
                    if (resp.getOk()) {
                        for (int i = 0; i < packSize; i++) {
                            uploadQueue.pollFirst();
                        }
                    } else {
                        sIsError = true;
                        ErrorsObservable.notify(new Exception(getString(R.string.upload_not_ok)));
                        break;
                    }
                } catch (Exception e) {
                    sIsError = true;
                    ErrorsObservable.notify(e, true);
                    break;
                }
            }
            if (uploadQueue.size() == 0) {
                completed = true;
                break;
            }
        }
        return completed;
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = cm.getActiveNetwork();
            if (nw == null) {
                return false;
            }
            NetworkCapabilities cap = cm.getNetworkCapabilities(nw);
            if (cap == null) {
                return false;
            }

            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true;
            }
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true;
            }
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true;
            }
            //noinspection RedundantIfStatement
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                return true;
            }
            return false;
        } else {
            @SuppressWarnings("deprecation") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            //noinspection deprecation
            return activeNetwork != null &&
                    activeNetwork.isConnected();
        }
    }

    private LocationOuterClass.Location marshall(Location location) {
        Intent batteryIntent = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status;
        int level;
        int scale;
        if (batteryIntent != null) {
            status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        } else {
            level = -1;
            scale = -1;
            status = 0;
        }

        return LocationOuterClass.Location.newBuilder()
                .setAcc(i(location.getAccuracy()))
                .setAlt(i(location.getAltitude()))
                .setBatp((level == -1 || scale == -1) ? 50 : Math.round((float) level * 100.0f / (float) scale))
                .setBats(status != 0)
                .setBear(location.hasBearing() ? i(location.getBearing()) : Bear.EMPTY_BEAR)
                .setLat(i(location.getLatitude()))
                .setLon(i(location.getLongitude()))
                .setProvider(location.getProvider())
                .setSid(randomUUID().toString())
                .setSpeed(i(location.getSpeed()))
                .setTid(mTrackID.get())
                .setTs(location.getTime() / 1000)// System.currentTimeMillis() / 1000)
                .build();
    }

    @VisibleForTesting()
    public TrackerGrpc.TrackerBlockingStub getGrpcStub() {
        return TrackerGrpc.newBlockingStub(ch);
    }

    public static void enqueueUploadLocation(Context context, Location location) {
        uploadQueue.add(location);
        Log.d(LT,"Add location to the queue, size=" + uploadQueue.size());
        notifyUpdateState();
        enqueueUploadLocations(context);
    }

    static private final int sJobID = 1001;

    public static void enqueueUploadLocations(Context context) {
        Intent intent = new Intent(context, UploadJobService.class);
        enqueueWork(context, UploadJobService.class, sJobID, intent);
    }


    private static Status sPrevStatus;
    private static void notifyUpdateState() {
        if (BuildConfig.DEBUG) {
            boolean changed = false;
            if (sIsError != sPrevIsError) {
                sPrevIsError = sIsError;
                changed = true;
            }
            if (sIsUploading != sPrevIsUploading) {
                sPrevIsUploading = sIsUploading;
                changed = true;
            }
            int size = uploadQueue.size();
            if (size != sPrevSize) {
                changed = true;
            }
            if (changed) {
                Status status = uploadStatus();
                if (sPrevStatus == status && sPrevSize == size) {
                    ErrorsObservable.notify(new Exception("Status must not be the same"), true);
                }
                sPrevStatus = status;
                sPrevSize = size;
                subjectStatus.onNext(uploadStatus());
            }
        } else {
            subjectStatus.onNext(uploadStatus());
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(LT,"Upload onHandleWork, queue size=" + uploadQueue.size());
        if (mTrackID.isNotSet()) return;
        if (sIsUploading) {
            ErrorsObservable.notify(new Error("UploadJobService re-entry"), BuildConfig.DEBUG);
        }
        sIsUploading = true;
        if (uploadQueue.size() > 0) {
            sIsError = false;

            notifyUpdateState();

            boolean completed = false;
            if (isConnected()) {
                uploadStore();
                completed = uploadQueue();
            } else {
                sIsError = true;
            }
            Log.d(LT,"Queue uploaded, size=" + uploadQueue.size()+", error="+sIsError);
            if (!completed) {
                if (uploadQueue.size() > MAX_LOCATIONS_MEMORY) {
                    saveQueueToStore();
                }
            }
            if (sIsError) {
                destoryGrpcChannel();
            }
            sIsUploading = false;
            notifyUpdateState();
        } else {
            sIsUploading = false;
        }
    }

}
