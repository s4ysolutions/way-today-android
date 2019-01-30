package solutions.s4y.waytoday.grpc;

import androidx.annotation.NonNull;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCPort;

public class GRPCChannelProvider {
    private PreferenceGRPCHost host;
    private PreferenceGRPCPort port;
    private ManagedChannel sChannel;
    private @NonNull
    String usedHost = "";
    private int usedPort;

    public GRPCChannelProvider(@NonNull PreferenceGRPCHost host, @NonNull PreferenceGRPCPort port) {
        this.host = host;
        this.port = port;
    }

    public ManagedChannel channel() {
        if (!usedHost.equals(host.get()) || usedPort != port.get()) {
            sChannel = null;
        }
        if (sChannel == null) {
            usedPort = port.get();
            String nulledHost = host.get();
            usedHost = nulledHost == null ? "" : nulledHost;
            sChannel = ManagedChannelBuilder
                    .forAddress(host.get(), port.get())
                    .usePlaintext(true)
                    .build();
        }
        return sChannel;
    }
}
