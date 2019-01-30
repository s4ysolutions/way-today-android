package solutions.s4y.waytoday.preferences.entries;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.preferences.BooleanPreference;

public class PreferenceIsTracking extends BooleanPreference {
    public PreferenceIsTracking(@NonNull SharedPreferences preferences) {
        super(preferences, "isTracking", false);
    }
}
