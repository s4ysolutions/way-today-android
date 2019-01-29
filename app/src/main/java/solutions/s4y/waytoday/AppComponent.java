package solutions.s4y.waytoday;

import javax.inject.Singleton;

import dagger.Component;
import solutions.s4y.waytoday.errors.ErrorsModule;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        ErrorsModule.class,
})

public interface AppComponent {
    void inject(WTApplication application);

    void inject(MainActivity activity);
}
