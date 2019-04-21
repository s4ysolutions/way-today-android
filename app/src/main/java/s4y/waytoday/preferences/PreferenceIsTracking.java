package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceIsTracking extends BaseBooleanPreference {
    public PreferenceIsTracking(@NonNull SharedPreferences preferences) {
        super(preferences, "active", true);
    }

    public boolean isOn() {
        return getBoxed();
    }

    public boolean isOff() {
        return !getBoxed();
    }
}
