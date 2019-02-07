package solutions.s4y.waytoday.wsse;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class WsseTest {
    @Test
    public void wsseGetToken_shouldReturnToken() {
        String token = Wsse.getToken();

        String[] headers = token.split(",");
        assertThat(headers.length).isEqualTo(4);
    }
}
