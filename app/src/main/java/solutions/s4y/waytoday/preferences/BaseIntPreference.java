package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class BaseIntPreference extends BasePreference<Integer> {
    BaseIntPreference(@NonNull SharedPreferences preferences, @NonNull String key, int defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, @NonNull Integer value) {
        editor.putInt(key, value);
    }

    @Override
    Integer getBoxed() {
        return preferences.getInt(key, defaultValue);
    }
}
