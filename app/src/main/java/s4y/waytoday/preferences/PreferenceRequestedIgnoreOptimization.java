package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceRequestedIgnoreOptimization extends BaseBooleanPreference {
    public PreferenceRequestedIgnoreOptimization(@NonNull SharedPreferences preferences) {
        super(preferences, "rio", false);
    }

    public boolean requested() {
        return getBoxed();
    }

    public void setRequested() {
        set(true);
    }
}
