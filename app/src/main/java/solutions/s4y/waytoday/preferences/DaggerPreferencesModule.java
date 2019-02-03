package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module()
public class DaggerPreferencesModule {
    @Provides
    @Singleton
    PreferenceTrackID providePreferenceTrackID(SharedPreferences sp) {
        return new PreferenceTrackID(sp);
    }

    @Provides
    @Singleton
    PreferenceGRPCHost providePreferenceGRPCHost(SharedPreferences sp) {
        return new PreferenceGRPCHost(sp);
    }

    @Provides
    @Singleton
    PreferenceGRPCPort providePreferenceGRPCPort(SharedPreferences sp) {
        return new PreferenceGRPCPort(sp);
    }

    @Provides
    @Singleton
    PreferenceUpdateFrequency provideUserStrategyUpdateFrequency(SharedPreferences sp) {
        return new PreferenceUpdateFrequency(sp);
    }
}
