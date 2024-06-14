package s4y.waytoday.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.gps.sdk.GPSUpdatesManager;
import s4y.gps.sdk.android.implementation.FusedGPSUpdatesProvider;
import s4y.gps.sdk.dependencies.IGPSUpdatesProvider;

@Module()
public class DaggerModuleGPS {
    @Provides
    @Singleton
    static GPSUpdatesManager provideGPSUpdatesManager(Context context) {
        IGPSUpdatesProvider gpsUpdatesProvider = new FusedGPSUpdatesProvider(context, null);
        // TODO: replace capacity with 1 after testing
        GPSUpdatesManager gpsUpdatesManager = new GPSUpdatesManager(gpsUpdatesProvider, 20);
        return gpsUpdatesManager;
    }

}
