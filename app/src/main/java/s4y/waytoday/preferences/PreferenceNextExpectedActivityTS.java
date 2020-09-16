package s4y.waytoday.preferences;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;


public class PreferenceNextExpectedActivityTS extends BaseLongPreference {
    public PreferenceNextExpectedActivityTS(@NonNull SharedPreferences preferences) {
        super(preferences, "neats",0);
    }

    public long get() {
        Long boxed = getBoxed();
        return boxed == null ? 0: boxed;
    }
}
