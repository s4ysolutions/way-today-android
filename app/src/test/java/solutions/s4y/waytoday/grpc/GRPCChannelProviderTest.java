package solutions.s4y.waytoday.grpc;

import org.junit.Test;

import io.grpc.ManagedChannel;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.entries.PreferenceGRPCPort;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GRPCChannelProviderTest {
    private PreferenceGRPCHost grpcHost = mock(PreferenceGRPCHost.class);
    private PreferenceGRPCPort grpcPort = mock(PreferenceGRPCPort.class);

    @Test
    public void consequenceCalls_returnsSameChannel() throws GRPCChannelProvider.PortNotSetException {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);

        when(grpcHost.getValue()).thenReturn("some");
        when(grpcPort.getValue()).thenReturn(9999);
        ManagedChannel ch1 = provider.channel();
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch1).isEqualTo(ch2);
    }

    @Test
    public void consequenceCallsWithDifferentHosts_returnsDifferentChannels() throws GRPCChannelProvider.PortNotSetException {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);
        when(grpcHost.getValue()).thenReturn("some");
        when(grpcPort.getValue()).thenReturn(9999);

        ManagedChannel ch1 = provider.channel();

        when(grpcHost.getValue()).thenReturn("another");
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch2).isNotNull();
        assertThat(ch1).isNotEqualTo(ch2);
    }


    @Test
    public void consequenceCallsWithDifferentPorts_returnsDifferentChannels() throws GRPCChannelProvider.PortNotSetException {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);
        when(grpcHost.getValue()).thenReturn("some");
        when(grpcPort.getValue()).thenReturn(9999);

        ManagedChannel ch1 = provider.channel();

        when(grpcPort.getValue()).thenReturn(8888);
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch2).isNotNull();
        assertThat(ch1).isNotEqualTo(ch2);
    }
}
