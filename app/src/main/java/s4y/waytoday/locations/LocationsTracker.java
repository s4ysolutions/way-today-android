package s4y.waytoday.locations;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.PublishSubject;
import mad.location.manager.lib.Filters.GPSAccKalmanFilter;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.grpc.LocationOuterClass;
import s4y.waytoday.strategies.Strategy;

public class LocationsTracker {
    public static final PublishSubject<Location> subjectLocations = PublishSubject.create();
    public static final PublishSubject<TrackingState> subjectTrackingState = PublishSubject.create();
    private static final String LT = LocationOuterClass.Location.class.getSimpleName();

    static private final LocationListener locationListener = new LocationListener() {

        private GPSAccKalmanFilter m_kalmanFilter;
        // private SensorDataEventLoopTask m_eventLoopTask;
        private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
        private SensorManager m_sensorManager;
        private double m_magneticDeclination = 0.0;

        private float[] rotationMatrix = new float[16];
        private float[] rotationMatrixInv = new float[16];
        private float[] absAcceleration = new float[4];
        private float[] linearAcceleration = new float[4];

        private Location mPrevLocation = null;

        @Override
        public void onLocationChanged(Location location) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onLocationChanged");
            }
            if (mPrevLocation != null) {
                if (
                        Math.abs(mPrevLocation.getLatitude() - location.getLatitude()) < 0.00005 ||
                                Math.abs(mPrevLocation.getLongitude() - location.getLongitude()) < 0.00005 ||
                                Math.abs(location.getLatitude()) < 0.0005 ||
                                Math.abs(location.getLongitude()) < 0.0005
                ) {
                    return;
                }
            }
            subjectLocations.onNext(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            isSuspended = false;
            subjectTrackingState.onNext(new TrackingState());
        }

        @Override
        public void onProviderDisabled(String provider) {
            isSuspended = true;
            subjectTrackingState.onNext(new TrackingState());
        }
    };

    public static boolean isSuspended = true;
    /*accelerometer + rotation vector*/
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };
    static private final RequestUpdatesListener requestListener = success -> {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onRequestResult: " + success);
        }
        isSuspended = !success;
        subjectTrackingState.onNext(new TrackingState());
    };
    public static boolean isUpdating;
    private static LocationsUpdater updater;

    public static void requestStart(@NonNull final LocationsUpdater updater, @NonNull final Strategy strategy) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "requestStart each " + strategy.getMinTime() + " ms");
        }
        stop();
        LocationsTracker.updater = updater;
        isUpdating = true;
        isSuspended = true;
        subjectTrackingState.onNext(new TrackingState());
        updater.requestLocationUpdates(strategy, locationListener, requestListener);
    }

    public static void stop() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "stop");
        }
        if (updater != null) {
            updater.cancelLocationUpdates(locationListener);
            updater = null;
        }
        if (isUpdating) {
            isUpdating = false;
            subjectTrackingState.onNext(new TrackingState());
        }
    }

    static public class TrackingState {
        final boolean isUpdating;
        final boolean isSuspended;

        TrackingState() {
            this.isUpdating = LocationsTracker.isUpdating;
            this.isSuspended = LocationsTracker.isSuspended;
        }
    }
}
