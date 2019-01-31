package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;


public class StringPreference extends LiveDataPreference<String> {

    protected StringPreference(@NonNull SharedPreferences preferences,
                               @NonNull String key,
                               String defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    String getValueFromPreferences() {
        return preferences.getString(key, defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, String value) {
        editor.putString(key, value);
    }
}
