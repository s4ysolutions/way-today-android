package solutions.s4y.waytoday.preferences.entries;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.BuildConfig;
import solutions.s4y.waytoday.preferences.StringPreference;

public class PreferenceGRPCHost extends StringPreference {
    public PreferenceGRPCHost(@NonNull SharedPreferences preferences) {
        super(preferences, "grpchost", BuildConfig.GRPC_HOST);
    }
}
