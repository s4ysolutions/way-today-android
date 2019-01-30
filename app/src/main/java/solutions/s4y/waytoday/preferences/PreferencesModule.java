package solutions.s4y.waytoday.preferences;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCPort;
import solutions.s4y.waytoday.preferences.entries.PreferenceIsTracking;
import solutions.s4y.waytoday.preferences.entries.PreferenceTrackID;

@Module()
public class PreferencesModule {
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
}
