package solutions.s4y.waytoday;

import android.app.Application;
import android.os.StrictMode;

public class WTApplication extends Application {
    static {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
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

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = prepareAppComponent();
        mAppComponent.inject(this);
    }

    protected AppComponent prepareAppComponent() {
        return DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this)).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

}
