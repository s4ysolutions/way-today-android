package solutions.s4y.waytoday;

import android.app.Application;

public class WTApplication extends Application {
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
