package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.BuildConfig;

public class PreferenceGRPCPort extends BaseIntPreference {
    public PreferenceGRPCPort(@NonNull SharedPreferences preferences) {
        super(preferences, "grpcport", BuildConfig.GRPC_PORT);
    }

    public int get() {
        Integer boxed = getBoxed();
        return boxed == null ? defaultValue: boxed;
    }
}
