package solutions.s4y.waytoday.preferences.entries;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.preferences.IntPreference;

public class PreferenceGRPCPort extends IntPreference {
    public PreferenceGRPCPort(@NonNull SharedPreferences preferences) {
        super(preferences, "grpchost", BuildConfig.GRPC_PORT);
    }
}
