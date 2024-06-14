package s4y.waytoday.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.preferences.PreferenceUpdateFrequency;
import s4y.waytoday.strategies.UserStrategy;

@Module
public class DaggerModuleStrategies {
    @Provides
    @Singleton
    UserStrategy providesUserStrategy(PreferenceUpdateFrequency preference) {
        return new UserStrategy(preference);
    }
}
