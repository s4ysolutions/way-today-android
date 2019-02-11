package solutions.s4y.waytoday;

import javax.inject.Singleton;

import dagger.Component;
import solutions.s4y.waytoday.grpc.DaggerGRPCChannelProviderModule;
import solutions.s4y.waytoday.idservice.IDServiceTest;
import solutions.s4y.waytoday.mainactivity.PermissionsOnFirstLaunchTest;

@Singleton
@Component(modules = {
        DaggerTestPreferencesModule.class,
        DaggerApplicationModule.class,
        DaggerTestErrorsModule.class,
        DaggerGRPCChannelProviderModule.class
})
public interface TestComponent extends AppComponent {
    void inject(PermissionsOnFirstLaunchTest test);
    void inject(IDServiceTest test);
}