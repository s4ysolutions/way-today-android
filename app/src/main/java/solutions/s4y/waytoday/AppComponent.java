package solutions.s4y.waytoday;

import javax.inject.Singleton;

import dagger.Component;
import solutions.s4y.waytoday.errors.ErrorsModule;
import solutions.s4y.waytoday.grpc.GRPCChannelProviderModule;
import solutions.s4y.waytoday.idservice.IDService;
import solutions.s4y.waytoday.preferences.PreferencesModule;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        ErrorsModule.class,
        PreferencesModule.class,
        GRPCChannelProviderModule.class
})

public interface AppComponent {
    void inject(WTApplication application);
    void inject(MainActivity activity);
    void inject(IDService service);

}
