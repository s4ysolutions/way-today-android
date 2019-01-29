package solutions.s4y.waytoday.errors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;

public class ErrorReporter { // for testing purpose
    private Toast mToast;

    private boolean isToasting() {
        return mToast != null && mToast.getView() != null && mToast.getView().isShown();
    }

    private void toastAndCrash(final Context context, final ErrorNotification err) {
        if (isToasting()) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, err.getMessage(), Toast.LENGTH_SHORT);
        mToast.show();
//        Crashlytics.logException(err.th);
    }

    public void errorToast(Context context, ErrorNotification err) {
        toastAndCrash(context, err);
        Log.e("WT message", err.getMessage(), err.th);
    }

    public void warnToast(Context context, Throwable th) {
        toastAndCrash(context, new ErrorNotification(th));
        Log.w("WT message", th);
    }
}
