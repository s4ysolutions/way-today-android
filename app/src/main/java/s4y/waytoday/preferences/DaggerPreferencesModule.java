package s4y.waytoday.preferences;

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

    @Provides
    @Singleton
    PreferenceIsTracking providePreferenceIsActive(SharedPreferences sp) {
        return new PreferenceIsTracking(sp);
    }

    @Provides
    @Singleton
    PreferenceSound providePreferenceSound(SharedPreferences sp) {
        return new PreferenceSound(sp);
    }

}
