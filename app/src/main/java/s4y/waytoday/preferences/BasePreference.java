package s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import io.reactivex.subjects.PublishSubject;

public abstract class BasePreference<T> {
    @NonNull
    public final SharedPreferences preferences;
    @NonNull
    final T defaultValue;
    @NonNull
    public final String key;
    public PublishSubject<T> subject = PublishSubject.create();

    BasePreference(@NonNull SharedPreferences preferences,
                   @NonNull String key,
                   @NonNull T defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    abstract void putValueToPreferencesEditor(SharedPreferences.Editor editor, @NonNull T value);

    abstract T getBoxed();

    public void set(T value) {
        SharedPreferences.Editor editor = preferences.edit();
        putValueToPreferencesEditor(editor, value);
        editor.apply();
        subject.onNext(value);
    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
