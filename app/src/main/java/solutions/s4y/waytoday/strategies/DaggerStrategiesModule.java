package solutions.s4y.waytoday.strategies;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import solutions.s4y.waytoday.preferences.PreferenceUserStrategyUpdateFrequency;

@Module
public class DaggerStrategiesModule {
    @Provides
    @Singleton
    UserStrategy providesUserStrategy(PreferenceUserStrategyUpdateFrequency preference) {
        return new UserStrategy(preference);
    }
}
