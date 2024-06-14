package s4y.waytoday.dagger;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.preferences.PreferenceNextExpectedActivityTS;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;

@Module()
public class DaggerModulePreferences {
    @Provides
    @Singleton
    static PreferenceUpdateFrequency provideUserStrategyUpdateFrequency(SharedPreferences sp) {
        return new PreferenceUpdateFrequency(sp);
    }

    @Provides
    @Singleton
    static PreferenceSound providePreferenceSound(SharedPreferences sp) {
        return new PreferenceSound(sp);
    }

    @Provides
    @Singleton
    static PreferenceNextExpectedActivityTS providePreferenceNextExpectedActivityTS (SharedPreferences sp) {
        return new PreferenceNextExpectedActivityTS(sp);
    }
}
