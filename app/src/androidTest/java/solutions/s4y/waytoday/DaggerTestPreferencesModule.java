package solutions.s4y.waytoday;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import solutions.s4y.waytoday.preferences.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.PreferenceGRPCPort;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;
import solutions.s4y.waytoday.preferences.PreferenceTrackID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Module
class DaggerTestPreferencesModule {
    @Provides
    @Singleton
    PreferenceIsTracking providePreferenceIsTracking(SharedPreferences sp) {
        return mock(PreferenceIsTracking.class);
    }

    @Provides
    @Singleton
    PreferenceTrackID providePreferenceTrackID(SharedPreferences sp) {
        PreferenceTrackID trackID = new PreferenceTrackID(sp);
        return spy(trackID);
    }

    @Provides
    @Singleton
    PreferenceGRPCHost providePreferenceGRPCHost(SharedPreferences sp) {
        return mock(PreferenceGRPCHost.class);
    }

    @Provides
    @Singleton
    PreferenceGRPCPort providePreferenceGRPCPort(SharedPreferences sp) {
        return mock(PreferenceGRPCPort.class);
    }
}
