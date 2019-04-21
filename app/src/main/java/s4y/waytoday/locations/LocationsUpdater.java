package s4y.waytoday.locations;

import android.location.LocationListener;

import androidx.annotation.NonNull;
import s4y.waytoday.strategies.Strategy;

public interface LocationsUpdater {
    void requestLocationUpdates(
            @NonNull Strategy strategy,
            @NonNull LocationListener locationListener,
            @NonNull RequestUpdatesListener requestListener);

    void cancelLocationUpdates(@NonNull LocationListener listener);
}
