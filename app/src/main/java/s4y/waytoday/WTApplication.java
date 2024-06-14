package s4y.waytoday;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
            GPSUpdatesForegroundService.setNotificationContentTitle("Stop WayToday tracking");
            GPSUpdatesForegroundService.setUseApplicationNotificationSmallIcon(true);
        }
    }

    @Override
    public void onTerminate() {
        appDisposables.dispose();
        androidWayToday.wtClient
                .removeErrorsListener(this::onAndroidWayTodayClientError);
        super.onTerminate();
    }

    private void onAndroidWayTodayClientError(WayTodayError error) {
        ErrorsObservable.notify(error, false);
    }
}
