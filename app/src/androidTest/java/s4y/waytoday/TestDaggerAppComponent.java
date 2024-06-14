package s4y.waytoday;

import javax.inject.Singleton;

import s4y.waytoday.dagger.DaggerAppComponent;
import s4y.waytoday.dagger.DaggerApplicationModule;
import s4y.waytoday.grpc.DaggerGRPCChannelProviderModule;
import s4y.waytoday.idservice.IDServiceTest;
import s4y.waytoday.mainactivity.PermissionsOnFirstLaunchTest;

@Singleton
@dagger.Component(modules = {
        DaggerTestPreferencesModule.class,
        DaggerApplicationModule.class,
        DaggerTestErrorsModule.class,
        DaggerGRPCChannelProviderModule.class
})
public interface TestDaggerAppComponent extends DaggerAppComponent {
    void inject(PermissionsOnFirstLaunchTest test);
    void inject(IDServiceTest test);
}