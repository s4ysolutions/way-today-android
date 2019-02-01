package solutions.s4y.waytoday.locations;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import io.reactivex.Observable;

class LocationsObservable {
    static Observable<Location> fromUpdater(@NonNull final LocationsUpdater updater, @NonNull final Strategy strategy) {
        return Observable.create(emitter -> {
            final LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    emitter.onNext(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            emitter.setCancellable(() -> updater.unregisterListener(listener));
            updater.requestLocationUpdates(strategy, listener);
        });
    }
}
