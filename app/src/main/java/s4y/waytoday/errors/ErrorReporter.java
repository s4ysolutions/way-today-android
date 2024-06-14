package s4y.waytoday.errors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ErrorReporter { // for testing purpose
    private Toast mToast;

    private boolean isToasting() {
        return mToast != null && mToast.getView() != null && mToast.getView().isShown();
    }

    public void report(Context context, ErrorNotification err) {
        if (err.toast) {
            if (isToasting()) {
                mToast.cancel();
            }
            mToast =
                    err.hasResourceID()
                            ? Toast.makeText(context, err.resourceID, Toast.LENGTH_SHORT)
                            : Toast.makeText(context, err.getMessage(), Toast.LENGTH_SHORT);
            mToast.show();
        }
        if (err.th != null) {
            Log.e("WT message", err.getMessage(), err.th);
            try {
                FirebaseCrashlytics.getInstance().recordException(err.th);
            } catch (IllegalStateException e) {
                // unit tests
                Log.w("WT message", e);
            }
        }
    }
}
