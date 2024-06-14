package s4y.waytoday.dagger;

import javax.inject.Singleton;

import s4y.waytoday.MainActivity;
import s4y.waytoday.WTApplication;

@Singleton
@dagger.Component(modules = {
        DaggerModuleApplication.class,
        DaggerModuleErrors.class,
        DaggerModulePreferences.class,
        DaggerModuleStrategies.class,
        DaggerModuleWayToday.class,
        DaggerModuleGPS.class
})

public interface DaggerAppComponent {
    void inject(WTApplication application);
    void inject(MainActivity activity);
}