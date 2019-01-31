package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceIsTracking extends BaseBooleanPreference {
    public PreferenceIsTracking(@NonNull SharedPreferences preferences) {
        super(preferences, "isTracking", true);
    }

    public boolean get() {
        Boolean boxed = getBoxed();
        return (boxed == null)? defaultValue : boxed;
    }
}
