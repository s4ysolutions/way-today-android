package s4y.waytoday;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import s4y.waytoday.errors.ErrorReporter;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.notifications.AppNotification;

public class WTApplication extends Application {
    static {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or
                    //.detectAll() //for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }

    private static final String LT = WTApplication.class.getSimpleName();

    private AppComponent mAppComponent;
    @Inject
    ErrorReporter errorReporter;

    private CompositeDisposable appDisposables = new CompositeDisposable();
    private AppNotification mAppNotification;

    public static WTApplication sApplication;
    private static FirebaseAnalytics sFirebaseAnalytics;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    protected AppComponent prepareAppComponent() {
        return DaggerAppComponent.builder()
                .daggerApplicationModule(new DaggerApplicationModule(this)).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public AppNotification getAppNotification() {
        return mAppNotification;
    }

    static public void fa(@NonNull final String event, Bundle bundle) {
        if (sApplication == null) return;
        if (sFirebaseAnalytics == null) {
            sFirebaseAnalytics = FirebaseAnalytics.getInstance(sApplication);
        }
        sFirebaseAnalytics.logEvent(event, bundle);
        if (BuildConfig.DEBUG) {
            Log.d(LT, "FA log " + event);
        }
    }

    static public void fa(@NonNull final String event) {
        fa(event, null);
    }

    static public void faRequestID() {
        fa("wt_request_id");
    }

    static public void faOn() {
        fa("wt_on");
    }

    static public void faOff() {
        fa("wt_off");
    }

    static public void faShare() {
        fa("wt_share");
    }

    static public void faVisit() {
        fa("wt_visit");
    }

    static public void faSoundOn() {
        fa("wt_sound_on");
    }

    static public void faSoundOff() {
        fa("wt_sound_off");
    }

    static public void faFreqFling(long freq) {
        fa("wt_freq_fling");
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "wt_freq");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Set update frequency");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "number");
        bundle.putLong(FirebaseAnalytics.Param.VALUE, freq);
        fa("wt_freq_set");
    }

    static public void faFreqTap(long freq) {
        fa("wt_freq_tap");
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "wt_freq");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Set update frequency");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "number");
        bundle.putLong(FirebaseAnalytics.Param.VALUE, freq);
        fa("wt_freq_set");
    }

    static public void faNoPermission() {
        fa("wt_no_permission");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        mAppComponent = prepareAppComponent();
        appDisposables.add(ErrorsObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(errorNotification -> errorReporter.report(this, errorNotification)));
        mAppComponent.inject(this);
        mAppNotification = new AppNotification(this);
    }

}
