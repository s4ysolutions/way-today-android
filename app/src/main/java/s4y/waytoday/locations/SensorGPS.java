package s4y.waytoday.locations;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import mad.location.manager.lib.Commons.Utils;
import s4y.waytoday.BuildConfig;
import s4y.waytoday.grpc.LocationOuterClass;
import s4y.waytoday.strategies.Strategy;

public class SensorGPS {
    private static final String LT = LocationOuterClass.Location.class.getSimpleName();

    public final BehaviorSubject<DataItemGPS> subjectGPS = BehaviorSubject.createDefault(
            new DataItemGPS( null)
    );
    public PublishSubject<TrackingState> subjectTrackingState = PublishSubject.create();

    private final LocationListener locationListener = new LocationListener() {
        double prevLat = 0;
        double prevLon = 0;

        public void onLocationChanged(@NonNull Location location) {
            if (BuildConfig.DEBUG) {
                Log.d(LT, "onLocationChanged");
            }

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

            DataItemGPS sdi = new DataItemGPS(location);
            subjectGPS.onNext(sdi);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            isSuspended = false;
            subjectTrackingState.onNext(new TrackingState(isUpdating, isSuspended));
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            isSuspended = true;
            subjectTrackingState.onNext(new TrackingState(isUpdating, isSuspended));
        }
    };

    public boolean isSuspended = true;
    public boolean isUpdating;

    private final RequestUpdatesListener requestListener = success -> {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "onRequestResult: " + success);
        }
        isSuspended = !success;
        subjectTrackingState.onNext(new TrackingState(isUpdating, isSuspended));
    };
    private LocationsUpdater updater;

    public void requestStart(@NonNull final LocationsUpdater updater, @NonNull final Strategy strategy) {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "requestStart each " + strategy.getMinTime() + " ms");
        }
        stop();
        this.updater = updater;
        isUpdating = true;
        isSuspended = true;
        subjectTrackingState.onNext(new TrackingState(isUpdating, isSuspended));
        updater.requestLocationUpdates(strategy, locationListener, requestListener);
    }

    public void stop() {
        if (BuildConfig.DEBUG) {
            Log.d(LT, "stop");
        }
        if (updater != null) {
            updater.cancelLocationUpdates(locationListener);
            updater = null;
        }
        if (isUpdating) {
            isUpdating = false;
            subjectTrackingState.onNext(new TrackingState(isUpdating, isSuspended));
        }
    }

    public DataItemGPS data() {
        return subjectGPS.getValue();
    }

    public static class TrackingState {
        final boolean isUpdating;
        final boolean isSuspended;

        TrackingState(boolean isUpdating, boolean isSuspended) {
            this.isUpdating = isUpdating;
            this.isSuspended = isSuspended;
        }
    }
}
