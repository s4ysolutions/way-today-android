package solutions.s4y.waytoday;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)

public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertEquals("solutions.s4y.waytoday", appContext.getPackageName());
    }

    @Test
    public void useAppContext2() {
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertEquals("solutions.s4y.waytoday", appContext.getPackageName());
    }

}
