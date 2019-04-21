package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class BaseStringPreference extends BasePreference<String> {
    BaseStringPreference(
            @NonNull SharedPreferences preferences,
            @NonNull String key,
            @NonNull String defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, @NonNull String value) {
        editor.putString(key, value);
    }

    @Override
    String getBoxed() {
        return preferences.getString(key, defaultValue);
    }
}
