package solutions.s4y.waytoday.upload;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.JobIntentService;
import io.grpc.ManagedChannel;
import solutions.s4y.waytoday.R;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.grpc.GRPCChannelProvider;
import solutions.s4y.waytoday.grpc.LocationOuterClass;
import solutions.s4y.waytoday.grpc.TrackerGrpc;
import solutions.s4y.waytoday.grpc.TrackerOuterClass;
import solutions.s4y.waytoday.preferences.PreferenceTrackID;
import solutions.s4y.waytoday.utils.Bear;

import static java.util.UUID.randomUUID;
import static solutions.s4y.waytoday.utils.FConv.i;

public class UploadJobService extends JobIntentService {
    private static final LinkedList<Location> uploadQueue = new LinkedList<>();
    private final static int MAX_LOCATIONS_MEMORY = 500;
    private final static int PACK_SIZE = 16;
    @Inject
    public GRPCChannelProvider grpcChannelProvider;
    protected ManagedChannel ch = null;
    @Inject
    PreferenceTrackID mTrackID;

    public static void enqueueUploadLocation(Context context, Location location) {
        synchronized (uploadQueue) {
            uploadQueue.add(location);
        }
        enqueueUploadLocations(context);
    }

    public static void enqueueUploadLocations(Context context) {
        RetryUploadAlarm.cancelRetryUploadAlarmmanager(context);
        Intent intent = new Intent(context, UploadJobService.class);
        enqueueWork(context, UploadJobService.class, 1000, intent);
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

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        RetryUploadAlarm.cancelRetryUploadAlarmmanager(this);

        boolean completed = false;
        if (isConnected()) {
            uploadStore();
            completed = uploadQueue();
        }
        if (!completed) {
            if (uploadQueue.size() > MAX_LOCATIONS_MEMORY) {
                saveQueueToStore();
            }
            destoryGrpcChannel();
            RetryUploadAlarm.startRetryUploadAlarmmanager(this);
        }
    }

    private void uploadStore() {
    }

    private void saveQueueToStore() {
        synchronized (uploadQueue) {
            while (uploadQueue.size() > MAX_LOCATIONS_MEMORY) {
                uploadQueue.pollFirst();
            }
        }
    }

    private boolean uploadQueue() {
        List<Location> pack = new ArrayList<>();
        boolean completed = false;
        for (; ; ) {
            pack.clear();
            int packSize;
            synchronized (uploadQueue) {
                packSize = Math.min(uploadQueue.size(), PACK_SIZE);
                for (int i = 0; i < packSize; i++) {
                    Location head = uploadQueue.peekFirst();
                    if (head != null) {
                        pack.add(head);
                    }
                }
            }
            if (pack.size() > 0) {
                TrackerOuterClass.AddLocationsRequest.Builder req =
                        TrackerOuterClass.AddLocationsRequest
                                .newBuilder();
                for (Location location : pack) {
                    req.addLocations(marshall(location));
                }
                if (ch == null) ch = grpcChannelProvider.channel();
                TrackerGrpc.TrackerBlockingStub grpcStub = getGrpcStub();
                TrackerOuterClass.AddLocationResponse resp = grpcStub.addLocations(req.build());
                if (resp.getOk()) {
                    for (int i = 0; i < packSize; i++) {
                        synchronized (uploadQueue) {
                            uploadQueue.pollFirst();
                        }
                    }
                } else {
                    ErrorsObservable.notify(new Exception(getString(R.string.upload_not_ok)));
                    break;
                }
            }
            synchronized (uploadQueue) {
                if (uploadQueue.size() == 0) {
                    completed = true;
                    break;
                }
            }
        }
        return completed;
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
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
                .setBatp((level == -1 || scale == -1) ? 50 : i(((float) level * 100.0f / (float) scale)))
                .setBats(status != 0)
                .setBear(location.hasBearing() ? i(location.getBearing()) : Bear.EMPTY_BEAR)
                .setLat(i(location.getLatitude()))
                .setLon(i(location.getLongitude()))
                .setProvider(location.getProvider())
                .setSid(randomUUID().toString())
                .setSpeed(i(location.getSpeed()))
                .setTid(mTrackID.get())
                .setTs(System.currentTimeMillis() / 1000)
                .build();
    }

    @VisibleForTesting()
    public TrackerGrpc.TrackerBlockingStub getGrpcStub() {
        return TrackerGrpc.newBlockingStub(ch);
    }

}
