package solutions.s4y.waytoday;

import javax.inject.Singleton;

import dagger.Component;
import solutions.s4y.waytoday.errors.DaggerErrorsModule;
import solutions.s4y.waytoday.grpc.DaggerGRPCChannelProviderModule;
import solutions.s4y.waytoday.idservice.IDService;
import solutions.s4y.waytoday.locations.LocationsService;
import solutions.s4y.waytoday.preferences.DaggerPreferencesModule;
import solutions.s4y.waytoday.strategies.DaggerStrategiesModule;
import solutions.s4y.waytoday.upload.UploadService;

@Singleton
@Component(modules = {
        DaggerApplicationModule.class,
        DaggerErrorsModule.class,
        DaggerPreferencesModule.class,
        DaggerStrategiesModule.class,
        DaggerGRPCChannelProviderModule.class
})

public interface AppComponent {
    void inject(WTApplication application);
    void inject(MainActivity activity);
    void inject(IDService service);

    void inject(UploadService service);
    void inject(LocationsService service);
}