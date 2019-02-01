package solutions.s4y.waytoday.errors;

import androidx.annotation.NonNull;

public class ErrorNotification {
    static final int NO_RESOURCE_ID = -1;
    final Throwable th;
    final String message;
    final boolean toast;
    final int resourceID;

    public ErrorNotification(@NonNull Throwable th, boolean toast) {
        this.th = th;
        this.message = getMessage();
        this.toast = toast;
        this.resourceID = NO_RESOURCE_ID;
    }

    public ErrorNotification(@NonNull Throwable th) {
        this(th, false);
    }

    public ErrorNotification(@NonNull String message, boolean toast) {
        this.th = null;
        this.message = message;
        this.toast = toast;
        this.resourceID = NO_RESOURCE_ID;
    }

    public ErrorNotification(int resourceID, boolean toast) {
        this.th = null;
        this.message = null;
        this.toast = toast;
        this.resourceID = resourceID;
    }

    public ErrorNotification(int resourceID) {
        this(resourceID, false);
    }

    public boolean hasResourceID() {
        return resourceID != NO_RESOURCE_ID;
    }

    public String getMessage() {
        if (message != null) {
            return null;
        } else if (th != null) {
            Throwable real = this.th;
            while (real.getCause() != null && real.getCause().getMessage() != null) {
                real = real.getCause();
            }
            return real.getMessage();
        } else {
            return "";
        }
    }
}