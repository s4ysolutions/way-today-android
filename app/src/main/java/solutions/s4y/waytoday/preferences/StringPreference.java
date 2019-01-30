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
    String getValueFromPreferences(String key, String defaultValue) {
        return getValue();
    }
}
