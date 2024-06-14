package s4y.waytoday.dagger;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.WTApplication;
import s4y.waytoday.analytics.Analytics;
import s4y.waytoday.analytics.DefaultAnalytics;

@Module()
public class DaggerModuleApplication {
    private final WTApplication application;

    public DaggerModuleApplication(WTApplication application) {
        this.application = application;
    }
    @Provides
    @Singleton
    static Context provideContext(WTApplication application) {
        return application;
    }

    @Provides
    @Singleton
    static SharedPreferences provideSharedPreferences(WTApplication application) {
        return getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    WTApplication provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    Analytics provideAnalytics(WTApplication application) {
        return new DefaultAnalytics(application);
    }
}
