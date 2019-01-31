package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceTrackID extends BaseStringPreference {
    public PreferenceTrackID(@NonNull SharedPreferences preferences) {
        super(preferences, "tid", "");
    }
    @NonNull
    public String get() {
        return getBoxed();
    }
}
