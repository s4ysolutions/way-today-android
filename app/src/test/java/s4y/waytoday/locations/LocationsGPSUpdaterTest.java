package s4y.waytoday.locations;

import android.content.Context;
import android.location.LocationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import s4y.waytoday.errors.ErrorsObservable;
import s4y.waytoday.permissions.PermissionRequestObservable;
import s4y.waytoday.strategies.RTStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(AndroidJUnit4.class)
public class LocationsGPSUpdaterTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    LocationListener locationListener = mock(LocationListener.class);
    RequestUpdatesListener requestUpdatesListener = mock(RequestUpdatesListener.class);

    Consumer errorsObserver = mock(Consumer.class);
    Consumer permissionsObserver = mock(Consumer.class);
    CompositeDisposable disposable;

    @Before
    public void setUp() {
        reset(locationListener);
        reset(requestUpdatesListener);
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

    @Test
    public void locationsGPSUpdater_shouldEmitNoGpsErrorAnOnRequestResultTrue() throws Exception {
        Context context = spy(ApplicationProvider.getApplicationContext());
        doReturn(null).when(context).getSystemService(Context.LOCATION_SERVICE);
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);

        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener, requestUpdatesListener);

        verify(requestUpdatesListener, times(1)).onRequestResult(true);
        verify(errorsObserver, times(1)).accept(any());
        verify(permissionsObserver, never()).accept(any());
        verify(locationListener, never()).onLocationChanged(any());
    }

    @Test
    @Ignore
    public void locationsGPSUpdater_shouldEmitPermissionRequest() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);

        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener, requestUpdatesListener);

        verify(errorsObserver, times(1)).accept(any());
        verify(permissionsObserver, times(1)).accept(any());
        verify(locationListener, never()).onLocationChanged(any());
    }
}
