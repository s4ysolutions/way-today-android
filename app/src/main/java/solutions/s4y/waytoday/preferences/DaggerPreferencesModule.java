package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module()
public class DaggerPreferencesModule {
    @Provides
    @Singleton
    PreferenceIsTracking providePreferenceIsTracking(SharedPreferences sp) {
        return new PreferenceIsTracking(sp);
    }

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
    PreferenceUserStrategyUpdateFrequency provideUserStrategyUpdateFrequency(SharedPreferences sp) {
        return new PreferenceUserStrategyUpdateFrequency(sp);
    }
}
