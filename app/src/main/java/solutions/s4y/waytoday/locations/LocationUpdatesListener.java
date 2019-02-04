package solutions.s4y.waytoday.locations;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import io.reactivex.subjects.PublishSubject;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.grpc.LocationOuterClass;
import solutions.s4y.waytoday.strategies.Strategy;

public class LocationUpdatesListener {
    public static final PublishSubject<Location> subjectLocations = PublishSubject.create();
    public static final PublishSubject<TrackingState> subjectTrackingState = PublishSubject.create();
    private static final String LT = LocationOuterClass.Location.class.getSimpleName();
    public static boolean isSuspended;
    static private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LT, "onLocationChanged");
            subjectLocations.onNext(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            ErrorsObservable.notify(
                    "LocationListener.onStatusChanged: " + status,
                    BuildConfig.DEBUG);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(LT, "onProviderEnabled");
            isSuspended = false;
            subjectTrackingState.onNext(new TrackingState());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(LT, "onProviderDisabled");
            isSuspended = true;
            subjectTrackingState.onNext(new TrackingState());
        }
    };
    static private final RequestUpdatesListener requestListener = new RequestUpdatesListener() {
        @Override
        public void onRequestResult(boolean success) {
            Log.d(LT, "onRequestResult: " + success);
            isSuspended = !success;
            subjectTrackingState.onNext(new TrackingState());
        }
    };
    public static boolean isUpdating;
    private static LocationsUpdater updater;

    public static void requestStart(@NonNull final LocationsUpdater updater, @NonNull final Strategy strategy) {
        Log.d(LT, "requestStart");
        stop();
        LocationUpdatesListener.updater = updater;
        isUpdating = true;
        isSuspended = false;
        subjectTrackingState.onNext(new TrackingState());
        updater.requestLocationUpdates(strategy, locationListener, requestListener);
    }

    public static void stop() {
        Log.d(LT, "stop");
        if (updater != null) {
            updater.unregisterListener(locationListener);
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
            this.isUpdating = LocationUpdatesListener.isUpdating;
            this.isSuspended = LocationUpdatesListener.isSuspended;
        }
    }
}
