package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class IntPreference extends LiveDataPreference<Integer> {

    protected IntPreference(@NonNull SharedPreferences preferences,
                            @NonNull String key,
                            int defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    Integer getValueFromPreferences(String key, Integer defaultValue) {
        return getValue();
    }
}
