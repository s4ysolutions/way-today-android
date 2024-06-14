package s4y.waytoday.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import solutions.s4y.waytoday.sdk.AndroidWayTodayClient;

@Module()
public class DaggerModuleWayToday {
    @Provides
    @Singleton
    static AndroidWayTodayClient provideAndroidWayTodayClient( Context context) {
        return new AndroidWayTodayClient(context, "waytoday-android", "waytoday-android-todo", "WayToday Android");
    }

}
