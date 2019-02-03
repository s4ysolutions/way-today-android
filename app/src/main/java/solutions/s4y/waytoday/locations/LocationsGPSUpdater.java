package solutions.s4y.waytoday.locations;

import android.Manifest;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import solutions.s4y.waytoday.R;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;
import solutions.s4y.waytoday.permissions.RestartOnGivenPermssion;
import solutions.s4y.waytoday.strategies.Strategy;

public class LocationsGPSUpdater implements LocationsUpdater {
    @VisibleForTesting
    public LocationManager mLocationManager;

    LocationsGPSUpdater(@NonNull Context context) {
        mLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void requestLocationUpdates(@NonNull Strategy strategy, @NonNull LocationListener listener) {
        new Restarter(strategy, listener).restart();
    }

    @Override
    public void unregisterListener(@NonNull LocationListener listener) {
        if (mLocationManager == null) {
            ErrorsObservable.toast(R.string.no_location_manager);
            return;
        }
        mLocationManager.removeUpdates(listener);
    }

    private class Restarter implements RestartOnGivenPermssion {
        @NonNull
        final Strategy mStrategy;
        @NonNull
        final LocationListener mLocationListener;

        Restarter(@NonNull Strategy mStrategy, @NonNull LocationListener mLocationListener) {
            this.mStrategy = mStrategy;
            this.mLocationListener = mLocationListener;
        }

        @Override
        public void restart() {
            if (mLocationManager == null) {
                ErrorsObservable.toast(R.string.no_location_manager);
                return;
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        mStrategy.getMinTime(),
                        mStrategy.getMinDistance(),
                        mLocationListener
                );
            } catch (SecurityException e) {
                ErrorsObservable.notify(e);
                PermissionRequestObservable.onNext(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        this
                );
            }
        }
    }

}
