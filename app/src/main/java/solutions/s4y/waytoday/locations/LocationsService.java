package solutions.s4y.waytoday.locations;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.disposables.Disposable;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;

import static solutions.s4y.waytoday.notifications.AppNotification.FOREGROUND_NOTIFICATION_ID;

public class LocationsService extends Service {
    static public final String FLAG_FOREGROUND = "ffg";
    @Inject
    PreferenceIsTracking mIsTracking;

    private LocationsUpdater gpsLocatonUpdater;
    private Disposable mLocationsObservable;
    private boolean mForeground;
    private boolean mStarted;

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
        return new Binder() {
            @NonNull
            LocationsService getService() {
                return LocationsService.this;
            }
        };
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
        if (mLocationsObservable != null) {
            ErrorsObservable.notify(new Exception("mLocationsObservable != null"), BuildConfig.DEBUG);
        }
        mLocationsObservable = LocationsObservable
                .fromUpdater(gpsLocatonUpdater, new RTStrategy())
                .subscribe();
    }

    void stopUpdateLocations() {
        if (mLocationsObservable != null) {
            mLocationsObservable.dispose();
            mLocationsObservable = null;
        } else {
            ErrorsObservable.notify(new Exception("mLocationsObservable == null"), BuildConfig.DEBUG);
        }
    }

    boolean isUpdatingLocation() {
        return mLocationsObservable != null;
    }

    void putInForeground() {
        Notification notification =
                ((WTApplication) getApplication())
                        .getAppNotification()
                        .notificationForForeground();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        mForeground = true;
    }

    void removeFromForeground() {
        stopForeground(true);
        mForeground = false;
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

    public void stop() {
        mStarted = false;
        if (isUpdatingLocation()) stopUpdateLocations();
        removeFromForeground();
    }
}
