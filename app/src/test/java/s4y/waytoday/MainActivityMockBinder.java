package s4y.waytoday;

import android.os.IBinder;

import s4y.waytoday.background.BackgroundService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainActivityMockBinder extends MainActivity {
    static BackgroundService.LocationsServiceBinder mockedBinder =
            mock(BackgroundService.LocationsServiceBinder.class);
    static BackgroundService mockedBackgroundService =
            mock(BackgroundService.class);

    static {
        when(mockedBinder.getService()).thenReturn(mockedBackgroundService);
    }

    @Override
    protected void setBinder(IBinder binder) {
        super.setBinder(mockedBinder);
    }
}
