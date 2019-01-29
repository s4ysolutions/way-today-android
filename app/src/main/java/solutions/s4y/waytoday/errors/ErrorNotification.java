package solutions.s4y.waytoday.errors;

import androidx.annotation.NonNull;

public class ErrorNotification {
    final Throwable th;

    public ErrorNotification(@NonNull Throwable th) {
        this.th = th;
    }

    public String getMessage() {
        Throwable real = this.th;
        while (real.getCause() != null && real.getCause().getMessage() != null) {
            real = real.getCause();
        }
        return real.getMessage();
    }
}