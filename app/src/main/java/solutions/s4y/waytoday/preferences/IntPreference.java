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
    Integer getValueFromPreferences() {
        return preferences.getInt(key, defaultValue == null ? 0 : defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, Integer value) {
        editor.putInt(key, value);
    }
}
