package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

abstract class LiveDataPreference<T> extends LiveData<T> {
    public final T defaultValue;
    protected final SharedPreferences preferences;
    private final String key;
    private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == LiveDataPreference.this.key) {
                setValue(getValueFromPreferences(key, defaultValue));
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

    abstract T getValueFromPreferences(String key, T defaultValue);

    @Override
    protected void onActive() {
        super.onActive();
        setValue(getValueFromPreferences(key, defaultValue));
        preferences.registerOnSharedPreferenceChangeListener(mChangeListener);
    }

    @Override
    protected void onInactive() {
        preferences.unregisterOnSharedPreferenceChangeListener(mChangeListener);
        super.onInactive();
    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }

}
