package s4y.waytoday;

import android.content.Context;

import s4y.waytoday.dagger.DaggerAppComponent;
import s4y.waytoday.dagger.DaggerApplicationModule;

public class TestApplication extends WTApplication {


    @Override
    protected DaggerAppComponent prepareAppComponent() {
        System.setProperty("org.mockito.android.target",
                this
                        .getDir("target", Context.MODE_PRIVATE)
                        .getAbsolutePath());
//                        .getCacheDir().getPath());

        return DaggerTestComponent
                .builder()
                .daggerApplicationModule(new DaggerApplicationModule(this))
                .build();
    }
}
