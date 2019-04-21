package s4y.waytoday.wsse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.google.common.truth.Truth.assertThat;

@Config(sdk = 16)
@RunWith(AndroidJUnit4.class)
public class WsseSdk16Test {
    @Test
    public void wsseGetToken_shouldReturnToken() {
        String token = Wsse.getToken();

        String[] headers = token.split(",");
        assertThat(headers.length).isEqualTo(4);
    }
}
