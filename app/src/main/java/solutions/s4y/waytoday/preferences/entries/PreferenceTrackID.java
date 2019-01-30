package solutions.s4y.waytoday.preferences.entries;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import solutions.s4y.waytoday.preferences.StringPreference;

public class PreferenceTrackID extends StringPreference {
    public PreferenceTrackID(@NonNull SharedPreferences preferences) {
        super(preferences, "tid", "");
    }

    public boolean isEmpty() {
        String v = getValue();
        return null == v || "".equals(v);
    }
}
