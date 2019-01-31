package solutions.s4y.waytoday;

import android.content.Context;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static com.google.common.truth.Truth.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)

public class ExampleInstrumentedTest {
    private class FooMocked {
        void foo() {

        }
    }
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        FooMocked fooMocked = mock(FooMocked.class);
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertThat("solutions.s4y.waytoday").isEqualTo(appContext.getPackageName());
//        assertEquals("solutions.s4y.waytoday", appContext.getPackageName());
        fooMocked.foo();
        verify(fooMocked).foo();
    }

    @Test
    public void useAppContext2() {
        FooMocked fooMocked = mock(FooMocked.class);
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertThat("solutions.s4y.waytoday").isEqualTo(appContext.getPackageName());
//        assertEquals("solutions.s4y.waytoday", appContext.getPackageName());
        fooMocked.foo();
        verify(fooMocked).foo();
    }

}
