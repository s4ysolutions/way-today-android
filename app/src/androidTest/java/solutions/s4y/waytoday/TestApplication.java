package solutions.s4y.waytoday;

import android.content.Context;

public class TestApplication extends WTApplication {


    @Override
    protected AppComponent prepareAppComponent() {
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
