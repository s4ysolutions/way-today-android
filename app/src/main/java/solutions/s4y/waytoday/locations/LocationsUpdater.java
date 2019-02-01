package solutions.s4y.waytoday.locations;

import android.location.LocationListener;

import androidx.annotation.NonNull;

public interface LocationsUpdater {
    void requestLocationUpdates(@NonNull Strategy strategy, @NonNull LocationListener listener);

    void unregisterListener(@NonNull LocationListener listener);
}
