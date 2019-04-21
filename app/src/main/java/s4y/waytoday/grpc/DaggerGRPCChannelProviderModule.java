package s4y.waytoday.grpc;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.preferences.PreferenceGRPCHost;
import s4y.waytoday.preferences.PreferenceGRPCPort;

@Module
public class DaggerGRPCChannelProviderModule {
    @Provides
    @Singleton
    GRPCChannelProvider provideGRPCChannelProvider(PreferenceGRPCHost host, PreferenceGRPCPort port) {
        return new GRPCChannelProvider(host, port);
    }
}
