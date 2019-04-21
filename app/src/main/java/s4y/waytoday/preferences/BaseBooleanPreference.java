package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class BaseBooleanPreference extends BasePreference<Boolean> {
    BaseBooleanPreference(@NonNull SharedPreferences preferences, @NonNull String key, boolean defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor,@NonNull Boolean value) {
        editor.putBoolean(key, value);
    }

    @Override
    Boolean getBoxed() {
        return preferences.getBoolean(key, defaultValue);
    }
}
