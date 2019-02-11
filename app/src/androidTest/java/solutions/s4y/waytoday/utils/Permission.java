package solutions.s4y.waytoday.utils;

import android.os.Build;

import java.io.IOException;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class Permission {
    private static final int BUTTON_GRANT_INDEX = 1;

    public static void allowPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                UiObject allowPermissions = device.findObject(new UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(BUTTON_GRANT_INDEX));
                if (allowPermissions.exists()) {
                    allowPermissions.click();
                }
            }
        } catch (UiObjectNotFoundException e) {
            System.out.println("There is no permissions dialog to interact with");
        }
    }

    public static void revokePermission() throws IOException {
        InstrumentationRegistry
                .getInstrumentation()
                .getUiAutomation()
                .executeShellCommand("pm revoke solutions.s4y.waytoday android.permission.ACCESS_FINE_LOCATION")
                .close();
    }
}
