package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

abstract class LiveDataPreference<T> extends LiveData<T> {
    public final T defaultValue;
    protected final SharedPreferences preferences;
    protected final String key;
    private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == LiveDataPreference.this.key) {
                setValue(getValueFromPreferences());
            }
        }
    };

    public LiveDataPreference(@NonNull SharedPreferences preferences,
                              @NonNull String key,
                              @Nullable T defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    abstract T getValueFromPreferences();

    abstract void putValueToPreferencesEditor(SharedPreferences.Editor editor, T value);

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getValueFromPreferences());
        preferences.registerOnSharedPreferenceChangeListener(mChangeListener);
    }

    @Override
    protected void onInactive() {
        preferences.unregisterOnSharedPreferenceChangeListener(mChangeListener);
        super.onInactive();
    }

    @Override
    public void setValue(T value) {
        SharedPreferences.Editor editor = preferences.edit();
        putValueToPreferencesEditor(editor, value);
        editor.apply();
        super.setValue(value);
    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }

}
