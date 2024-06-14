package s4y.waytoday.analytics;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import s4y.waytoday.BuildConfig;

public class DefaultAnalytics implements Analytics {
    private final Context context;

    public DefaultAnalytics(Context context) {
        this.context = context;
    }

    @Override
    public void fa(@NonNull String event, Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.d("ANALYTICS", event);
        }
        FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
    }
    public void fa(@NonNull final String event) {
        fa(event, null);
    }

    public void faRequestID() {
        fa("wt_request_id");
    }

    public void faOn() {
        fa("wt_on");
    }

    public void faOff() {
        fa("wt_off");
    }

    public void faShare() {
        fa("wt_share");
    }

    public void faVisit() {
        fa("wt_visit");
    }

    public void faSoundOn() {
        fa("wt_sound_on");
    }

    public void faSoundOff() {
        fa("wt_sound_off");
    }

    public void faFreqFling(long freq) {
        fa("wt_freq_fling");
        Bundle bundle = new Bundle();
        bundle.putString(Analytics.Param.ITEM_ID, "wt_freq");
        bundle.putString(Analytics.Param.ITEM_NAME, "Set update frequency");
        bundle.putString(Analytics.Param.CONTENT_TYPE, "number");
        bundle.putLong(Analytics.Param.VALUE, freq);
        fa("wt_freq_set");
    }

    public void faFreqTap(long freq) {
        fa("wt_freq_tap");
        Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_ID, "wt_freq");
        bundle.putString(Param.ITEM_NAME, "Set update frequency");
        bundle.putString(Param.CONTENT_TYPE, "number");
        bundle.putLong(Param.VALUE, freq);
        fa("wt_freq_set");
    }

    public void faNoPermission() {
        fa("wt_no_permission");
    }

}
