package s4y.waytoday.background;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import mad.location.manager.lib.Commons.Coordinates;
import mad.location.manager.lib.Commons.GeoPoint;
import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Filters.GPSAccKalmanFilter;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.MainActivity;
import s4y.waytoday.WTApplication;
import s4y.waytoday.locations.DataItem;
import s4y.waytoday.locations.DataItemAcc;
import s4y.waytoday.locations.DataItemGPS;
import s4y.waytoday.locations.FilterSettings;
import s4y.waytoday.locations.LocationsGPSUpdater;
import s4y.waytoday.locations.SensorGPS;
import s4y.waytoday.locations.LocationsUpdater;
import s4y.waytoday.preferences.PreferenceIsTracking;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.locations.SensorAcc;
import s4y.waytoday.sound.MediaPlayerUtils;
import s4y.waytoday.strategies.RTStrategy;
import s4y.waytoday.strategies.Strategy;
import s4y.waytoday.strategies.UserStrategy;
import s4y.waytoday.upload.UploadJobService;

import static s4y.waytoday.notifications.AppNotification.FOREGROUND_NOTIFICATION_ID;
import static s4y.waytoday.upload.UploadJobService.enqueueUploadLocation;

public class BackgroundService extends Service {
    static public final String LT = BackgroundService.class.getSimpleName();
    //    static public final String WL_TAG = "s4y.waytoday:backgground_service";
    static public final String FLAG_FOREGROUND = "ffg";
    static public Strategy currentStrategy = new RTStrategy();

    private LocationsUpdater gpsLocatonUpdater;
    private SensorAcc sensorAcc;
    static public SensorGPS sensorGPS = new SensorGPS();
    private GPSAccKalmanFilter kalmanFilter;
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

    // private PowerManager.WakeLock wakeLock;

    private FilterSettings filterSettings = FilterSettings.defaultSettings;

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
        // PowerManager powerManager = (PowerManager) WTApplication.sApplication.getSystemService(POWER_SERVICE);
        // wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WL_TAG);
        currentStrategy = new UserStrategy(mPreferenceUpdateFrequency);
        sensorAcc = new SensorAcc(this);
        gpsLocatonUpdater = new LocationsGPSUpdater(this);
        mServiceDisposables = new CompositeDisposable();
        mServiceDisposables.add(
                sensorGPS
                        .subjectGPS
                        .subscribe(this::onGPS));
        mServiceDisposables.add(
                sensorAcc
                        .subjectAcc
                        .subscribe(this::onAcc));
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
        sensorGPS.stop();
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

    @SuppressLint("WakelockTimeout")
    void startUpdateLocations() {
        // wakeLock.acquire();
        // NOTE: too much power
        // sensorAcc.startListen();
        sensorGPS.requestStart(gpsLocatonUpdater, currentStrategy);
    }

    void stopUpdateLocations() {
        sensorGPS.stop();
        // sensorAcc.stopListen();
        kalmanFilter = null;
        /*
        if (wakeLock.isHeld())
            wakeLock.release();
         */
    }

    double lastGPSTimeStamp = 0;

    private void onGPS(DataItemGPS gps) {
        if (BuildConfig.DEBUG) {
            Log.d("LT", "onGPS " + ((gps.location == null) ? "null" : gps.location.toString()));
        }
        if (gps.location == null) {
            return;
        }
        if (gps.location.getLatitude() == 0 || gps.location.getLongitude() == 0) {
            return;
        }
        if (kalmanFilter == null) {
            Location location = gps.location;
            double x, y, xVel, yVel, posDev, course, speed;
            long timeStamp;
            speed = location.getSpeed();
            course = location.getBearing();
            x = location.getLongitude();
            y = location.getLatitude();
            xVel = speed * Math.cos(course);
            yVel = speed * Math.sin(course);
            posDev = location.getAccuracy();
            timeStamp = Utils.nano2milli(location.getElapsedRealtimeNanos());
            kalmanFilter = new GPSAccKalmanFilter(
                    false, //todo move to settings
                    Coordinates.longitudeToMeters(x),
                    Coordinates.latitudeToMeters(y),
                    xVel,
                    yVel,
                    filterSettings.accelerationDeviation,
                    posDev,
                    timeStamp,
                    filterSettings.mVelFactor,
                    filterSettings.mPosFactor);
            DataItemAcc acc = sensorAcc.data();
            if (acc.absNorthAcc == DataItem.NOT_INITIALIZED) {
                acc = SensorAcc.zero;
            }
            handlePredict(acc);
        }

        double ts = gps.getTimestamp();
        if (ts < lastGPSTimeStamp) {
            return;
        }
        lastGPSTimeStamp = ts;

        handleUpdate(gps, gps.location);
        Location location = locationAfterUpdateStep(gps.location);
        publishLocation(location);
    }

    private double lastAccTimeStamp = 0;

    private void onAcc(DataItemAcc acc) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onAcc east=" + acc.absEastAcc + " nort=" + acc.absNorthAcc);
        }
        if (acc.absNorthAcc == DataItem.NOT_INITIALIZED) {
            return;
        }
        if (kalmanFilter != null) {
            if (acc.getTimestamp() < lastAccTimeStamp) {
                return;
            }
            lastAccTimeStamp = acc.getTimestamp();

            handlePredict(acc);
        }
    }

    private void handlePredict(DataItemAcc acc) {
        DataItemGPS gps = sensorGPS.data();
        if (gps.location == null) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "kalmanFilter.predict (no location): east=" + acc.absEastAcc + " north=" + acc.absNorthAcc);
            }
            kalmanFilter.predict(acc.getTimestamp(), acc.absEastAcc, acc.absNorthAcc);
        } else {
            double declination = gps.getDeclination();
            if (BuildConfig.DEBUG) {
                Log.d(LT, "kalmanFilter.predict: (location): east=" + acc.getAbsEastAcc(declination) + " north=" + acc.getAbsNorthAcc(declination) + " decl=" + declination);
            }
            kalmanFilter.predict(acc.getTimestamp(), acc.getAbsEastAcc(declination), acc.getAbsNorthAcc(declination));
        }
    }

    private void handleUpdate(DataItemGPS gps, Location location) {
        double xVel = location.getSpeed() * Math.cos(location.getBearing());
        double yVel = location.getSpeed() * Math.sin(location.getBearing());

        if (BuildConfig.DEBUG) {
            if (gps.location != null) {
                Log.d(LT, "kalmanFilter.update: lon=" + gps.location.getLongitude() + " lat=" + gps.location.getLatitude() + " xVel=" + xVel + " yVel=" + yVel);
            }
        }

        kalmanFilter.update(
                gps.getTimestamp(),
                Coordinates.longitudeToMeters(location.getLongitude()),
                Coordinates.latitudeToMeters(location.getLatitude()),
                xVel,
                yVel,
                location.getAccuracy(),
                gps.getVelErr()
        );
    }

    private Location locationAfterUpdateStep(Location location) {
        double xVel, yVel;
        Location loc = new Location("WayTodayAnroidKalman");
        GeoPoint pp = Coordinates.metersToGeoPoint(kalmanFilter.getCurrentX(),
                kalmanFilter.getCurrentY());
        loc.setLatitude(pp.Latitude);
        loc.setLongitude(pp.Longitude);
        loc.setAltitude(location.getAltitude());
        xVel = kalmanFilter.getCurrentXVel();
        yVel = kalmanFilter.getCurrentYVel();
        double speed = Math.sqrt(xVel * xVel + yVel * yVel); //scalar speed without bearing
        loc.setBearing(location.getBearing());
        loc.setSpeed((float) speed);
        loc.setTime(System.currentTimeMillis());
        loc.setElapsedRealtimeNanos(System.nanoTime());
        loc.setAccuracy(location.getAccuracy());

        if (BuildConfig.DEBUG) {
            Log.d(LT, "locationAfterUpdateStep: " + loc.getLongitude() + "," + loc.getLatitude());
        }

        return loc;
    }

    double prevLat = 0;
    double prevLon = 0;

    private void publishLocation(Location location) {

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if (lat == 0 || lon == 0) {
            return;
        }

        if (Math.abs(lat - prevLat) < 0.0001 && Math.abs(lon - prevLon) < 0.0001) {
            return;
        }

        prevLon = lon;
        prevLat = lat;

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
        if (sensorGPS.isUpdating) {
            stopUpdateLocations();
        }
        startUpdateLocations();
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
