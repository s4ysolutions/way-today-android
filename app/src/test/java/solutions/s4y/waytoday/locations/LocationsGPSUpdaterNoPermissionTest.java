package solutions.s4y.waytoday.locations;

import android.app.Application;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import solutions.s4y.waytoday.MainActivityMockBinder;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.permissions.PermissionRequestObservable;
import solutions.s4y.waytoday.strategies.RTStrategy;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(AndroidJUnit4.class)
public class LocationsGPSUpdaterNoPermissionTest {
    @Rule
    public ActivityTestRule<MainActivityMockBinder> mActivityRule =
            new ActivityTestRule(MainActivityMockBinder.class);

    LocationManager mockLocationManager = mock(LocationManager.class);
    LocationListener mockLocationListener = mock(LocationListener.class);
    RequestUpdatesListener mockRequestUpdatesListener = mock(RequestUpdatesListener.class);

    Consumer errorsObserver = mock(Consumer.class);
    Consumer permissionsObserver = mock(Consumer.class);
    CompositeDisposable disposable;

    @Before
    public void setUp() {
        reset(mockLocationManager);
        reset(mockLocationListener);
        reset(mockRequestUpdatesListener);
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
    public void activity_shouldNotHaveFineLocationPermission() {
        int result = mActivityRule.getActivity()
                .checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        assertThat(result).isEqualTo(PERMISSION_DENIED);
    }

    @Test
    public void shadowContext_shouldProvideMockedSystemService() {
        Application context = ApplicationProvider.getApplicationContext();
        ShadowApplication shadowContext = Shadows.shadowOf(context);
        shadowContext.setSystemService(Context.LOCATION_SERVICE, mockLocationManager);

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assertThat(manager).isEqualTo(mockLocationManager);
    }

    @Test
    public void locationsGPSUpdater_shouldEmitPermissionRequestAndonRequestResultFalse() throws Exception {
        Application context = ApplicationProvider.getApplicationContext();
        ShadowApplication shadowContext = Shadows.shadowOf(context);
        shadowContext.setSystemService(Context.LOCATION_SERVICE, mockLocationManager);
        LocationsGPSUpdater locationsGPSUpdater = new LocationsGPSUpdater(context);

        doThrow(SecurityException.class)
                .when(mockLocationManager)
                .requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        new RTStrategy().getMinTime(),
                        new RTStrategy().getMinDistance(),
                        mockLocationListener);

        locationsGPSUpdater.requestLocationUpdates(new RTStrategy(), mockLocationListener, mockRequestUpdatesListener);

        verify(mockRequestUpdatesListener, times(1)).onRequestResult(false);
        verify(errorsObserver, times(1)).accept(any());
        verify(permissionsObserver, times(1)).accept(any());
        verify(mockLocationListener, never()).onLocationChanged(any());
    }
}
