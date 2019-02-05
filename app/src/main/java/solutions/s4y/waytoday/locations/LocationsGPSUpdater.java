package solutions.s4y.waytoday.locations;

import android.Manifest;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.MainActivity;
import solutions.s4y.waytoday.R;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;
import solutions.s4y.waytoday.permissions.RestartOnGivenPermssion;
import solutions.s4y.waytoday.strategies.Strategy;

public class LocationsGPSUpdater implements LocationsUpdater {
    private static final String LT = LocationUpdatesListener.class.getSimpleName();
    @VisibleForTesting
    LocationManager mLocationManager;

    public LocationsGPSUpdater(@NonNull Context context) {
        mLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void requestLocationUpdates(
            @NonNull Strategy strategy,
            @NonNull LocationListener locationListener,
            @NonNull RequestUpdatesListener requestUpdatesListener
    ) {
        new Restarter(strategy, locationListener, requestUpdatesListener).restart();
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
        @NonNull
        final RequestUpdatesListener mRequestUpdatesListener;

        Restarter(@NonNull Strategy strategy,
                  @NonNull LocationListener locationListener,
                  @NonNull RequestUpdatesListener requestUpdatesListener) {
            mStrategy = strategy;
            mLocationListener = locationListener;
            mRequestUpdatesListener = requestUpdatesListener;
        }

        @Override
        public void restart() {
            if (mLocationManager == null) {
                ErrorsObservable.toast(R.string.no_location_manager);
                mRequestUpdatesListener.onRequestResult(false);
                return;
            }
            if (BuildConfig.DEBUG) {
                Log.d(LT, "requestLocationUpdates " + mStrategy.getMinTime() + " ms");
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        mStrategy.getMinTime(),
                        mStrategy.getMinDistance(),
                        mLocationListener
                );
                mRequestUpdatesListener.onRequestResult(true);
            } catch (IllegalArgumentException e) {
                ErrorsObservable.toast(e);
                mRequestUpdatesListener.onRequestResult(false);
            } catch (SecurityException e) {
                ErrorsObservable.notify(e, !MainActivity.sHasFocus);
                PermissionRequestObservable.onNext(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        this
                );
                mRequestUpdatesListener.onRequestResult(false);
            }
        }
    }

}
