package solutions.s4y.waytoday.locations;

import android.location.LocationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(AndroidJUnit4.class)
public class LocationsGPSUpdaterTest {
//    @Rule
//    public GrantPermissionRule mRuntimePermissionRule =
//            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    LocationListener locationListener = mock(LocationListener.class);
    Consumer errorsObserver = mock(Consumer.class);
    Consumer permissionsObserver = mock(Consumer.class);
    CompositeDisposable disposable;

    @Before
    public void setUp() {
        reset(locationListener);
        reset(errorsObserver);
        reset(permissionsObserver);
        disposable = new CompositeDisposable();
        disposable.add(
                PermissionRequestObservable
                        .subject
                        .subscribe(permissionsObserver));
        disposable.add(
                ErrorsObservable
                        .subject
                        .subscribe(errorsObserver));
    }

    @After
    public void tearDown() {
        disposable.clear();
    }
/*
    @Test
    public void locationsGPSUpdater_shouldEmitNoGpsError() throws Exception {
        Context context = spy(ApplicationProvider.getApplicationContext());
        doReturn(null).when(context).getSystemService(Context.LOCATION_SERVICE);
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);

        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener);

        verify(errorsObserver, times(1)).accept(any());
        verify(permissionsObserver, never()).accept(any());
        verify(locationListener, never()).onLocationChanged(any());
    }

    @Test
    @Ignore
    public void locationsGPSUpdater_shouldEmitPermissionRequest() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);

        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener);

        verify(errorsObserver, times(1)).accept(any());
        verify(permissionsObserver, times(1)).accept(any());
        verify(locationListener, never()).onLocationChanged(any());
    }
    */
}
