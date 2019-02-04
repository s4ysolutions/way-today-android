package solutions.s4y.waytoday.locations;

import android.location.LocationListener;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.strategies.Strategy;

public interface LocationsUpdater {
    void requestLocationUpdates(
            @NonNull Strategy strategy,
            @NonNull LocationListener locationListener,
            @NonNull RequestUpdatesListener requestListener);
    void unregisterListener(@NonNull LocationListener listener);
}
