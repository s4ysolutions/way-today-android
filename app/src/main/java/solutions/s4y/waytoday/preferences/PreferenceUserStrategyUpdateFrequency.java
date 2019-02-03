package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.strategies.UserStrategy;

public class PreferenceUserStrategyUpdateFrequency extends BaseStringPreference {
    PreferenceUserStrategyUpdateFrequency(@NonNull SharedPreferences preferences) {
        super(preferences, "freq", UserStrategy.UpdateFrequency.SEC1.toString());
    }

    public UserStrategy.UpdateFrequency get() {
        String s = super.getBoxed();
        return UserStrategy.UpdateFrequency.valueOf(s);
    }

    void set(@NonNull UserStrategy.UpdateFrequency value) {
        set(value.toString());
    }
}
