package s4y.waytoday.background;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import s4y.waytoday.MainActivity;
import s4y.waytoday.WTApplication;
import s4y.waytoday.locations.LocationsGPSUpdater;
import s4y.waytoday.locations.LocationsTracker;
import s4y.waytoday.locations.LocationsUpdater;
import s4y.waytoday.preferences.PreferenceIsTracking;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.sound.MediaPlayerUtils;
import s4y.waytoday.strategies.RTStrategy;
import s4y.waytoday.strategies.Strategy;
import s4y.waytoday.strategies.UserStrategy;
import s4y.waytoday.upload.UploadJobService;

import static s4y.waytoday.notifications.AppNotification.FOREGROUND_NOTIFICATION_ID;
import static s4y.waytoday.upload.UploadJobService.enqueueUploadLocation;

public class BackgroundService extends Service {
    static public final String FLAG_FOREGROUND = "ffg";
    static public Strategy currentStrategy = new RTStrategy();
    private LocationsUpdater gpsLocatonUpdater;
    @Inject
    PreferenceIsTracking mIsTracking;
    @Inject
    PreferenceTrackID mTrackID;
    @Inject
    PreferenceSound mSound;
    @Inject
    PreferenceUpdateFrequency mPreferenceUpdateFrequency;

    private boolean mForeground;
    private CompositeDisposable mServiceDisposables;

    public static void startService(Context context, boolean foreground) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.putExtra(BackgroundService.FLAG_FOREGROUND, foreground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((WTApplication) getApplication()).getAppComponent().inject(this);
        currentStrategy = new UserStrategy(mPreferenceUpdateFrequency);
        gpsLocatonUpdater = new LocationsGPSUpdater(this);
        mServiceDisposables = new CompositeDisposable();
        mServiceDisposables.add(
                LocationsTracker
                        .subjectLocations
                        .subscribe(this::onLocation));
        mServiceDisposables.add(
                UploadJobService
                        .subjectStatus
                        .subscribe(status -> handleUploadStatus()));
        mServiceDisposables.add(
                mPreferenceUpdateFrequency
                        .subject
                        .subscribe(freq -> restartUpdateLocations()));
        if (mIsTracking.isOn()) {
            start(!MainActivity.sHasFocus);
        }
    }

    @Override
    public void onDestroy() {
        mServiceDisposables.clear();
        LocationsTracker.stop();
        RetryUploadAlarm.cancelRetryUploadAlarmmanager(this);
        super.onDestroy();
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return new LocationsServiceBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        boolean foreground = intent == null
                ? mForeground
                : intent.getBooleanExtra(FLAG_FOREGROUND, false);
        start(foreground);
        return START_STICKY;
    }

    void startUpdateLocations() {
        LocationsTracker.requestStart(gpsLocatonUpdater, currentStrategy);
    }

    void stopUpdateLocations() {
        LocationsTracker.stop();
    }

    void onLocation(Location location) {
        RetryUploadAlarm.cancelRetryUploadAlarmmanager(this);
        if (mTrackID.isNotSet())
            return;
        enqueueUploadLocation(this, location);
        if (mSound.isOn()) {
            MediaPlayerUtils.getInstance(this).playGpsOk(this);
        }
    }

    public void removeFromForeground() {
        if (!mForeground)
            return;
        stopForeground(true);
        mForeground = false;
    }

    public void putInForeground() {
        if (mForeground)
            return;
        Notification notification =
                ((WTApplication) getApplication())
                        .getAppNotification()
                        .notificationForForeground();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        mForeground = true;
    }

    public void stop() {
        stopUpdateLocations();
        removeFromForeground();
        stopSelf();
    }

    public void start(boolean foreground) {
        if (foreground && !mForeground) {
            putInForeground();
        } else if (!foreground && mForeground) {
            removeFromForeground();
        }
        startUpdateLocations();
    }


    private void restartUpdateLocations() {
        if (LocationsTracker.isUpdating) {
            stopUpdateLocations();
            startUpdateLocations();
        }
    }

    public class LocationsServiceBinder extends Binder {
        @NonNull
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private void handleUploadStatus() {
        UploadJobService.Status status = UploadJobService.uploadStatus();
        if (mSound.isOn()) {
            if (status == UploadJobService.Status.ERROR)
                MediaPlayerUtils.getInstance(this).playUploadFail(this);
            else if (status == UploadJobService.Status.EMPTY)
                MediaPlayerUtils.getInstance(this).playUploadOk(this);
        }
        if (status == UploadJobService.Status.ERROR) {
            RetryUploadAlarm.startRetryUploadAlarmmanager(this);
        } else {
            // should never be used but just in case
            RetryUploadAlarm.cancelRetryUploadAlarmmanager(this);
        }
    }


}
