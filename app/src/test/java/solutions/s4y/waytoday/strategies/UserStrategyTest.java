package solutions.s4y.waytoday.strategies;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class UserStrategyTest {
    @Test
    public void userStrategy_shouldReturnCorrectPrevAndNext() {
        assertThat(UserStrategy.getPrev(null))
                .isEqualTo(null);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.SEC1))
                .isEqualTo(null);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.SEC5))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC1);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.SEC15))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC5);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.MIN1))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC15);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.MIN5))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN1);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.MIN30))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN15);
        assertThat(UserStrategy.getPrev(UserStrategy.UpdateFrequency.HOUR1))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN30);

        assertThat(UserStrategy.getNext(null))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC1);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.SEC1))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC5);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.SEC5))
                .isEqualTo(UserStrategy.UpdateFrequency.SEC15);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.SEC15))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN1);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.MIN1))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN5);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.MIN5))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN15);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.MIN15))
                .isEqualTo(UserStrategy.UpdateFrequency.MIN30);
        assertThat(UserStrategy.getNext(UserStrategy.UpdateFrequency.MIN30))
                .isEqualTo(UserStrategy.UpdateFrequency.HOUR1);
    }
}
