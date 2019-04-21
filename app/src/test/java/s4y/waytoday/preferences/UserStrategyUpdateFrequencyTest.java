package s4y.waytoday.preferences;

import org.junit.Test;

import s4y.waytoday.R;

import static com.google.common.truth.Truth.assertThat;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.HOUR1;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.MIN1;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.MIN15;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.MIN30;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.MIN5;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.SEC1;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.SEC15;
import static s4y.waytoday.preferences.PreferenceUpdateFrequency.Frequencies.SEC5;

public class UserStrategyUpdateFrequencyTest {
    @Test
    public void userStrategy_shouldReturnCorrectPrev() {
        assertThat(SEC1.getPrev())
                .isNull();
        assertThat(SEC5.getPrev())
                .isEqualTo(SEC1);
        assertThat(SEC15.getPrev())
                .isEqualTo(SEC5);
        assertThat(MIN1.getPrev())
                .isEqualTo(SEC15);
        assertThat(MIN5.getPrev())
                .isEqualTo(MIN1);
        assertThat(MIN15.getPrev())
                .isEqualTo(MIN5);
        assertThat(MIN30.getPrev())
                .isEqualTo(MIN15);
        assertThat(HOUR1.getPrev())
                .isEqualTo(MIN30);
    }

    @Test
    public void userStrategy_shouldReturnCorrectNext() {
        assertThat(SEC1.getNext())
                .isEqualTo(SEC5);
        assertThat(SEC5.getNext())
                .isEqualTo(SEC15);
        assertThat(SEC15.getNext())
                .isEqualTo(MIN1);
        assertThat(MIN1.getNext())
                .isEqualTo(MIN5);
        assertThat(MIN5.getNext())
                .isEqualTo(MIN15);
        assertThat(MIN15.getNext())
                .isEqualTo(MIN30);
        assertThat(MIN30.getNext())
                .isEqualTo(HOUR1);
        assertThat(HOUR1.getNext())
                .isNull();
    }

    @Test
    public void userStrategy_shouldReturnCorrectResID() {
        assertThat(SEC1.getTitleResID())
                .isEqualTo(R.string.freq_sec_1);
        assertThat(SEC5.getTitleResID())
                .isEqualTo(R.string.freq_sec_5);
        assertThat(SEC15.getTitleResID())
                .isEqualTo(R.string.freq_sec_15);
        assertThat(MIN1.getTitleResID())
                .isEqualTo(R.string.freq_min_1);
        assertThat(MIN5.getTitleResID())
                .isEqualTo(R.string.freq_min_5);
        assertThat(MIN15.getTitleResID())
                .isEqualTo(R.string.freq_min_15);
        assertThat(MIN30.getTitleResID())
                .isEqualTo(R.string.freq_min_30);
        assertThat(HOUR1.getTitleResID())
                .isEqualTo(R.string.freq_hour_1);
    }
}
