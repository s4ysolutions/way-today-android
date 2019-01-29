package solutions.s4y.waytoday;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module()
class ApplicationModule {
    private final WTApplication application;

    ApplicationModule(WTApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application application() {
        return application;
    }

}
