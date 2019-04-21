package s4y.waytoday.grpc;

import androidx.annotation.NonNull;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import s4y.waytoday.preferences.PreferenceGRPCHost;
import s4y.waytoday.preferences.PreferenceGRPCPort;

public class GRPCChannelProvider {
    private final boolean tls;
    private final PreferenceGRPCHost host;
    private final PreferenceGRPCPort port;

    GRPCChannelProvider(@NonNull PreferenceGRPCHost host, @NonNull PreferenceGRPCPort port) {
        this.host = host;
        this.port = port;
        tls = (port.get() % 1000) > 100;
    }

    public ManagedChannel channel() {
        ManagedChannelBuilder channelBuilder = ManagedChannelBuilder
                .forAddress(host.get(), port.get());
        if (!tls)
            channelBuilder.usePlaintext();
        return channelBuilder.build();
    }
}
