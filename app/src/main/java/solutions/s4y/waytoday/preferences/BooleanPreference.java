package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class BooleanPreference extends LiveDataPreference<Boolean> {

    protected BooleanPreference(@NonNull SharedPreferences preferences,
                                @NonNull String key,
                                Boolean defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    Boolean getValueFromPreferences() {
        return preferences.getBoolean(key, defaultValue == null ? false : defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, Boolean value) {
        editor.putBoolean(key, value);
    }
}
