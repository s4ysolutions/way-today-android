package s4y.waytoday.strategies;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;

@Module
public class DaggerStrategiesModule {
    @Provides
    @Singleton
    UserStrategy providesUserStrategy(PreferenceUpdateFrequency preference) {
        return new UserStrategy(preference);
    }
}
