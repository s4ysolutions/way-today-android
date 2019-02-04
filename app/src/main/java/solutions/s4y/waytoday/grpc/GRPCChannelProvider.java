package solutions.s4y.waytoday.grpc;

import androidx.annotation.NonNull;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import solutions.s4y.waytoday.preferences.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.PreferenceGRPCPort;

public class GRPCChannelProvider {
    private PreferenceGRPCHost host;
    private PreferenceGRPCPort port;

    GRPCChannelProvider(@NonNull PreferenceGRPCHost host, @NonNull PreferenceGRPCPort port) {
        this.host = host;
        this.port = port;
    }

    public ManagedChannel channel() {
        return ManagedChannelBuilder
                .forAddress(host.get(), port.get())
                .usePlaintext(true)
                .build();
    }
}
