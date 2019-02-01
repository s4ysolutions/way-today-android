package solutions.s4y.waytoday.idservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.JobIntentService;
import io.grpc.ManagedChannel;
import solutions.s4y.waytoday.AppComponent;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.grpc.GRPCChannelProvider;
import solutions.s4y.waytoday.grpc.TrackerGrpc;
import solutions.s4y.waytoday.grpc.TrackerOuterClass;
import solutions.s4y.waytoday.preferences.PreferenceTrackID;


public class IDService extends JobIntentService {
    private static final String EXTRA_PREVID = "previd";
    static private boolean sProgress = false;
    @Inject
    public GRPCChannelProvider grpcChannelProvider;
    protected ManagedChannel ch = null;
    @Inject
    PreferenceTrackID trackID;

    static synchronized public boolean isProgress() {
        return sProgress;
    }

    static synchronized public void setProgress(boolean progress) {
        sProgress = progress;
    }

    public static void enqueueRetrieveId(Context context, String prevID) {
        if (!isProgress()) {
            Intent intent = new Intent(context, IDService.class);
            intent.putExtra(EXTRA_PREVID, prevID);
            enqueueWork(context, IDService.class, 1000, intent);
        }
    }

    private void reportFail(Throwable e) {
        ErrorsObservable.notify(e);
    }

    protected boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    @VisibleForTesting()
    public TrackerGrpc.TrackerBlockingStub getGrpcStub() {
        return TrackerGrpc.newBlockingStub(ch);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppComponent component = ((WTApplication) getApplication()).getAppComponent();
        component.inject(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (isConnected()) {
            setProgress(true);

            String id = intent.getStringExtra(EXTRA_PREVID);

            try {
                // intent service can be handle work few times
                // without destory, te use the channel then
                if (ch == null) ch = grpcChannelProvider.channel();
                TrackerGrpc.TrackerBlockingStub grpcStub = getGrpcStub();

                TrackerOuterClass.GenerateTrackerIDRequest req = TrackerOuterClass.
                        GenerateTrackerIDRequest.
                        newBuilder()
                        .setPrevTid(id == null ? "" : id)
                        .build();

                TrackerOuterClass.GenerateTrackerIDResponse response = grpcStub.generateTrackerID(req);
                final String tid = response.getTid();
                trackID.set(tid);
            } catch (final Exception e) {
                reportFail(e);
            }
            setProgress(false);
        }
    }

    @VisibleForTesting()
    public void destoryGrpcChannel() {
        if (ch != null) {
            try {
                ch.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        destoryGrpcChannel();
        super.onDestroy();
    }

}
