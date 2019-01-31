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
    private Integer usedPort;

    public ManagedChannel channel() throws PortNotSetException {
        if (!usedHost.equals(host.getValue()) || !usedPort.equals(port.getValue())) {
            sChannel = null;
        }
        if (sChannel == null) {
            usedPort = port.getValue();
            String nulledHost = host.getValue();
            usedHost = nulledHost == null ? "" : nulledHost;
            if (port.getValue() == null) {
                throw new PortNotSetException();
            }
            sChannel = ManagedChannelBuilder
                    .forAddress(host.getValue(), port.getValue())
                    .usePlaintext(true)
                    .build();
        }
        return sChannel;
    }

    public GRPCChannelProvider(@NonNull PreferenceGRPCHost host, @NonNull PreferenceGRPCPort port) {
        this.host = host;
        this.port = port;
    }

    static class PortNotSetException extends Exception {
        PortNotSetException() {
            super("GRPC port is not set");
        }
    }
}
