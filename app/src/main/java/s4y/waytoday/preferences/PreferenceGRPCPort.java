package s4y.waytoday.preferences;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import s4y.waytoday.BuildConfig;

public class PreferenceGRPCPort extends BaseIntPreference {
    public PreferenceGRPCPort(@NonNull SharedPreferences preferences) {
        super(preferences,
                "grpcport",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? BuildConfig.GRPC_PORT
                        : BuildConfig.GRPC_PORT_PLAIN);
    }

    public int get() {
        Integer boxed = getBoxed();
        return boxed == null ? defaultValue: boxed;
    }
}
