package solutions.s4y.waytoday.background;

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
import solutions.s4y.waytoday.MainActivity;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.locations.LocationUpdatesListener;
import solutions.s4y.waytoday.locations.LocationsGPSUpdater;
import solutions.s4y.waytoday.locations.LocationsUpdater;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;
import solutions.s4y.waytoday.strategies.RTStrategy;
import solutions.s4y.waytoday.strategies.Strategy;

import static solutions.s4y.waytoday.notifications.AppNotification.FOREGROUND_NOTIFICATION_ID;
import static solutions.s4y.waytoday.upload.UploadJobService.enqueueUploadLocation;

public class BackgroundService extends Service {
    static public final String FLAG_FOREGROUND = "ffg";
    static public Strategy currentStrategy = new RTStrategy();
    private LocationsUpdater gpsLocatonUpdater;
    @Inject
    PreferenceIsTracking mIsTracking;
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
        gpsLocatonUpdater = new LocationsGPSUpdater(this);
        mServiceDisposables = new CompositeDisposable();
        mServiceDisposables.add(
                LocationUpdatesListener
                        .subjectLocations
                        .subscribe(this::onLocation));
        if (mIsTracking.isOn()) {
            start(!MainActivity.sHasFocus);
        }
    }

    @Override
    public void onDestroy() {
        mServiceDisposables.clear();
        LocationUpdatesListener.stop();
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
        LocationUpdatesListener.requestStart(gpsLocatonUpdater, currentStrategy);
    }

    void stopUpdateLocations() {
        LocationUpdatesListener.stop();
    }

    void onLocation(Location location) {
        enqueueUploadLocation(this, location);
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

    public class LocationsServiceBinder extends Binder {
        @NonNull
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
