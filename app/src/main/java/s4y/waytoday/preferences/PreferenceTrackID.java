package s4y.waytoday.preferences;

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

    public boolean isNotSet() {
        return "".equals(getBoxed());
    }

    public boolean isSet() {
        return !"".equals(getBoxed());
    }
}
