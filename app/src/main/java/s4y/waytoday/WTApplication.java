package s4y.waytoday;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import s4y.gps.sdk.GPSUpdate;
import s4y.gps.sdk.android.GPSUpdatesForegroundService;
import s4y.waytoday.dagger.DaggerAppComponent;
import s4y.waytoday.dagger.DaggerDaggerAppComponent;
import s4y.waytoday.dagger.DaggerModuleApplication;
import s4y.waytoday.errors.ErrorReporter;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.sound.MediaPlayerUtils;
import solutions.s4y.waytoday.sdk.AndroidWayTodayClient;
import solutions.s4y.waytoday.sdk.ITrackIdChangeListener;
import solutions.s4y.waytoday.sdk.IUploadingLocationsStatusChangeListener;
import solutions.s4y.waytoday.sdk.UploadingLocationsStatus;
import solutions.s4y.waytoday.sdk.WayTodayError;

public class WTApplication extends Application {
    static {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or
                    //.detectAll() //for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }

    private DaggerAppComponent mDaggerAppComponent;
    @Inject
    ErrorReporter errorReporter;
    @Inject
    AndroidWayTodayClient androidWayToday;
    @Inject
    PreferenceUpdateFrequency mUserStrategyFrequency;
    @Inject
    PreferenceSound mSound;

    private final CompositeDisposable appDisposables = new CompositeDisposable();

    protected s4y.waytoday.dagger.DaggerAppComponent prepareAppComponent() {
        return DaggerDaggerAppComponent
                .builder()
                .daggerModuleApplication(new DaggerModuleApplication(this)).build();
    }

    public s4y.waytoday.dagger.DaggerAppComponent getDaggerComponent() {
        return mDaggerAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDaggerAppComponent = prepareAppComponent();
        mDaggerAppComponent.inject(this);

        MediaPlayerUtils.setPreferenceSound(mSound);

        appDisposables.add(ErrorsObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(errorNotification -> errorReporter.report(this, errorNotification)));
        androidWayToday.wtClient.addErrorsListener(this::onAndroidWayTodayClientError);

        PreferenceUpdateFrequency.Frequencies current = mUserStrategyFrequency.get();
        androidWayToday.gpsUpdatesManager.setIntervalSec(current.getSeconds());

        GPSUpdatesForegroundService
                .setUpdatesManager(androidWayToday.gpsUpdatesManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GPSUpdatesForegroundService.setNotificationChannelId("waytoday_gps_updates");
            GPSUpdatesForegroundService.setNotificationChannelName("WayToday GPS Updates");
            // GPSUpdatesForegroundService.setNotificationContentTitle("");
            // GPSUpdatesForegroundService.setNotificationContent("WayToday Tracking");
            GPSUpdatesForegroundService.setUseApplicationNotificationSmallIcon(true);
        }

        androidWayToday.wtClient.addTrackIdChangeListener(updateTrackIDWithSound);
        androidWayToday.gpsUpdatesManager.getLast().addListener(onGPSUpdate);
        androidWayToday.wtClient.addUploadingLocationsStatusChangeListener(this.onUploadStatusChanged);
    }

    @Override
    public void onTerminate() {
        appDisposables.dispose();
        androidWayToday.wtClient
                .removeErrorsListener(this::onAndroidWayTodayClientError);
        androidWayToday.wtClient
                .removeTrackIdChangeListener(updateTrackIDWithSound);
        androidWayToday.gpsUpdatesManager.getLast()
                .removeListener(onGPSUpdate);
        androidWayToday.wtClient
                .removeUploadingLocationsStatusChangeListener(this.onUploadStatusChanged);
        super.onTerminate();
    }

    private void onAndroidWayTodayClientError(WayTodayError error) {
        ErrorsObservable.notify(error, false);
    }

    private final ITrackIdChangeListener updateTrackIDWithSound = trackID -> MediaPlayerUtils.getInstance(this).playTrackID(this);

    private final Function1<GPSUpdate, Unit> onGPSUpdate = (GPSUpdate gpsUpdate) -> {
        MediaPlayerUtils.getInstance(this).playGpsOk(this);
        return Unit.INSTANCE;
    };
    private final IUploadingLocationsStatusChangeListener onUploadStatusChanged= ignored -> {
        UploadingLocationsStatus status = androidWayToday.wtClient.getUploadingLocationsStatus();
        switch (status) {
            case QUEUED:
            case UPLOADING:
                break;
            case ERROR:
                MediaPlayerUtils.getInstance(this).playUploadFail(this);
                break;
            case EMPTY:
                MediaPlayerUtils.getInstance(this).playUploadOk(this);
        }
    };
}
