package s4y.waytoday.grpc;

import org.junit.Test;

import io.grpc.ManagedChannel;
import s4y.waytoday.preferences.PreferenceGRPCHost;
import s4y.waytoday.preferences.PreferenceGRPCPort;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GRPCChannelProviderTest {
    private PreferenceGRPCHost grpcHost = mock(PreferenceGRPCHost.class);
    private PreferenceGRPCPort grpcPort = mock(PreferenceGRPCPort.class);

    @Test
    public void consequenceCalls_returnsSameChannel() {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);

        when(grpcHost.get()).thenReturn("some");
        when(grpcPort.get()).thenReturn(9999);
        ManagedChannel ch1 = provider.channel();
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch1).isEqualTo(ch2);
    }

    @Test
    public void consequenceCallsWithDifferentHosts_returnsDifferentChannels() {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);
        when(grpcHost.get()).thenReturn("some");
        when(grpcPort.get()).thenReturn(9999);

        ManagedChannel ch1 = provider.channel();

        when(grpcHost.get()).thenReturn("another");
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch2).isNotNull();
        assertThat(ch1).isNotEqualTo(ch2);
    }


    @Test
    public void consequenceCallsWithDifferentPorts_returnsDifferentChannels() {
        GRPCChannelProvider provider = new GRPCChannelProvider(grpcHost, grpcPort);
        when(grpcHost.get()).thenReturn("some");
        when(grpcPort.get()).thenReturn(9999);

        ManagedChannel ch1 = provider.channel();

        when(grpcPort.get()).thenReturn(8888);
        ManagedChannel ch2 = provider.channel();

        assertThat(ch1).isNotNull();
        assertThat(ch2).isNotNull();
        assertThat(ch1).isNotEqualTo(ch2);
    }
}
