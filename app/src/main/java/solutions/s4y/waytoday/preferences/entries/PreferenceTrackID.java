package solutions.s4y.waytoday.preferences.entries;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.subjects.PublishSubject;
import solutions.s4y.waytoday.preferences.StringPreference;

public class PreferenceTrackID extends StringPreference {
    public PublishSubject<String> publishSubject = PublishSubject.create();

    public PreferenceTrackID(@NonNull SharedPreferences preferences) {
        super(preferences, "tid", "");
    }

    public boolean isEmpty() {
        String v = get();
        return null == v || "".equals(v);
    }

    public void set(@Nullable String value) {
        String prev = get();
        super.set(value);
        publishSubject.onNext(prev == null ? "" : prev);
    }
}
