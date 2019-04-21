package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import s4y.waytoday.BuildConfig;

public class PreferenceGRPCHost extends BaseStringPreference {
    public PreferenceGRPCHost(@NonNull SharedPreferences preferences) {
        super(preferences, "grpchost", BuildConfig.GRPC_HOST);
    }

    @NonNull
    public String get() {
        String boxed = getBoxed();
        return boxed == null ? "" : boxed;
    }
}
