package s4y.waytoday.locations;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import io.reactivex.subjects.PublishSubject;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.grpc.LocationOuterClass;
import s4y.waytoday.strategies.Strategy;

public class LocationsTracker {
    public static final PublishSubject<Location> subjectLocations = PublishSubject.create();
    public static final PublishSubject<TrackingState> subjectTrackingState = PublishSubject.create();
    private static final String LT = LocationOuterClass.Location.class.getSimpleName();
    public static boolean isSuspended = true;
    static private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onLocationChanged");
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
