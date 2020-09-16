package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class BaseLongPreference extends BasePreference<Long> {
    BaseLongPreference(@NonNull SharedPreferences preferences, @NonNull String key, long defaultValue) {
        super(preferences, key, defaultValue);
    }

    @Override
    void putValueToPreferencesEditor(SharedPreferences.Editor editor, @NonNull Long value) {
        editor.putLong(key, value);
    }

    @Override
    Long getBoxed() {
        return preferences.getLong(key, defaultValue);
    }
}
