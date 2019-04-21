package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceSound extends BaseBooleanPreference {
    public PreferenceSound(@NonNull SharedPreferences preferences) {
        super(preferences, "sound", false);
    }

    public boolean isOn() {
        return getBoxed();
    }

    public boolean isOff() {
        return !getBoxed();
    }
}
