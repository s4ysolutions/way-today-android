package solutions.s4y.waytoday;

import android.app.Application;
import android.os.StrictMode;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import solutions.s4y.waytoday.errors.ErrorReporter;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.notifications.AppNotification;

public class WTApplication extends Application {
    static {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    //.detectDiskReads()
                    //.detectDiskWrites()
                    //.detectNetwork()   // or
                    .detectAll() //for all detectable problems
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

    private AppComponent mAppComponent;
    @Inject
    ErrorReporter errorReporter;

    private CompositeDisposable appDisposables = new CompositeDisposable();
    private AppNotification mAppNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = prepareAppComponent();
        appDisposables.add(ErrorsObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(errorNotification -> errorReporter.report(this, errorNotification)));
        mAppComponent.inject(this);
        mAppNotification = new AppNotification(this);
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
}
