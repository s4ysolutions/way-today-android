package solutions.s4y.waytoday.locations;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.preferences.PreferenceTrackID;
import solutions.s4y.waytoday.preferences.PreferenceUpdateFrequency;
import solutions.s4y.waytoday.strategies.RTStrategy;
import solutions.s4y.waytoday.strategies.Strategy;

import static solutions.s4y.waytoday.notifications.AppNotification.FOREGROUND_NOTIFICATION_ID;
import static solutions.s4y.waytoday.upload.UploadService.enqueueUploadLocation;

public class LocationsService extends Service {
    static public final PublishSubject<Boolean> subjectTracking = PublishSubject.create();
    static public final String FLAG_FOREGROUND = "ffg";
    static public Strategy currentStrategy = new RTStrategy();
    @Inject
    PreferenceUpdateFrequency mUpdateFrequency;
    @Inject
    PreferenceTrackID mTrackID;

    private LocationsUpdater gpsLocatonUpdater;
    private Disposable mLocationsObservable;
    private boolean mForeground;
    private boolean mStarted;

    public static void startService(Context context, boolean foreground) {
        Intent intent = new Intent(context, LocationsService.class);
        intent.putExtra(LocationsService.FLAG_FOREGROUND, foreground);
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
        gpsLocatonUpdater = new LocationsGPSUpdater(this);
    }

    @Override
    public void onDestroy() {
        if (mLocationsObservable != null) {
            ErrorsObservable
                    .notify(new Exception("mLocationsObservable != null"), BuildConfig.DEBUG);
        }
        super.onDestroy();
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return new LocationsServiceBinder();
    }

    void startUpdateLocations() {
        if (mLocationsObservable != null) {
            ErrorsObservable.notify(new Exception("mLocationsObservable != null"), BuildConfig.DEBUG);
        }
        mLocationsObservable = LocationsObservable
                .fromUpdater(gpsLocatonUpdater, currentStrategy)
                .subscribe(location -> enqueueUploadLocation(this, location));
        subjectTracking.onNext(isUpdatingLocation());
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

    void stopUpdateLocations() {
        if (mLocationsObservable != null) {
            mLocationsObservable.dispose();
            mLocationsObservable = null;
        } else {
            ErrorsObservable.notify(new Exception("mLocationsObservable == null"), BuildConfig.DEBUG);
        }
        subjectTracking.onNext(isUpdatingLocation());
    }

    public boolean isUpdatingLocation() {
        return mLocationsObservable != null;
    }

    public void removeFromForeground() {
        stopForeground(true);
        mForeground = false;
    }

    void putInForeground() {
        Notification notification =
                ((WTApplication) getApplication())
                        .getAppNotification()
                        .notificationForForeground();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        mForeground = true;
    }

    public void stop() {
        mStarted = false;
        if (isUpdatingLocation()) stopUpdateLocations();
        removeFromForeground();
        stopSelf();
    }

    public void start(boolean foreground) {
        mStarted = true;
        if (foreground && !mForeground) {
            putInForeground();
        } else if (!foreground && mForeground) {
            removeFromForeground();
        }
        if (!isUpdatingLocation()) startUpdateLocations();
    }

    public class LocationsServiceBinder extends Binder {
        @NonNull
        public LocationsService getService() {
            return LocationsService.this;
        }
    }
}
