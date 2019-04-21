package s4y.waytoday;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.preferences.PreferenceGRPCHost;
import s4y.waytoday.preferences.PreferenceGRPCPort;
import s4y.waytoday.preferences.PreferenceIsTracking;
import s4y.waytoday.preferences.PreferenceSound;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;

import static org.mockito.Mockito.spy;

@Module
class DaggerTestPreferencesModule {
    @Provides
    @Singleton
    PreferenceIsTracking providePreferenceIsTracking(SharedPreferences sp) {
        PreferenceIsTracking pref = new PreferenceIsTracking(sp);
        return spy(pref);
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
        PreferenceGRPCHost pref = new PreferenceGRPCHost(sp);
        return spy(pref);
    }

    @Provides
    @Singleton
    PreferenceGRPCPort providePreferenceGRPCPort(SharedPreferences sp) {
        PreferenceGRPCPort pref = new PreferenceGRPCPort(sp);
        return spy(pref);
    }

    @Provides
    @Singleton
    PreferenceUpdateFrequency providePreferenceUpdate(SharedPreferences sp) {
        PreferenceUpdateFrequency pref = new PreferenceUpdateFrequency(sp);
        return spy(pref);
    }

    @Provides
    @Singleton
    PreferenceSound providePreferenceSound(SharedPreferences sp) {
        PreferenceSound pref = new PreferenceSound(sp);
        return spy(pref);
    }

}
