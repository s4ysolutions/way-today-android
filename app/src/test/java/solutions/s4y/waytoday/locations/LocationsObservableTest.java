package solutions.s4y.waytoday.locations;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.shadows.ShadowLocationManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;
import solutions.s4y.waytoday.strategies.RTStrategy;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(AndroidJUnit4.class)
public class LocationsObservableTest {
    Context context = ApplicationProvider.getApplicationContext();
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
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);
        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener);
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
    public void locationsGPSUpdater_shouldEmitNoGpsError() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);
        ShadowLocationManager shadowLocationManager =
                shadowOf(locationsGPSUpdater.mLocationManager);
        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), locationListener);

        Location l1 = location(GPS_PROVIDER, 12.0, 20.0);

        shadowLocationManager.simulateLocation(l1);

        verify(errorsObserver, never()).accept(any());
        verify(permissionsObserver, never()).accept(any());
        ArgumentCaptor<Location> lc = ArgumentCaptor.forClass(Location.class);
        verify(locationListener, times(1)).onLocationChanged(lc.capture());
        assertThat(GPS_PROVIDER).isEqualTo(lc.getValue().getProvider());
    }

    private Location location(String provider, double latitude, double longitude) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(System.currentTimeMillis());
        return location;
    }
}
