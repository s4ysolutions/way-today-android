package s4y.waytoday.mainactivity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import s4y.waytoday.MainActivity;
import s4y.waytoday.R;
import s4y.waytoday.TestComponent;
import s4y.waytoday.WTApplication;
import s4y.waytoday.locations.SensorGPS;
import s4y.waytoday.permissions.PermissionRequestObservable;
import s4y.waytoday.preferences.PreferenceIsTracking;
import s4y.waytoday.preferences.PreferenceTrackID;
import s4y.waytoday.utils.Admin;
import s4y.waytoday.utils.Permission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static s4y.waytoday.utils.Permission.revokePermission;

@SuppressWarnings("unchecked")
public class PermissionsOnFirstLaunchTest {
    @Inject
    PreferenceTrackID trackID;

    @Inject
    PreferenceIsTracking isTracking;

    private CompositeDisposable disposable;
    private Consumer mockErrorObserver = mock(Consumer.class);
    private Consumer mockPermissinObserver = mock(Consumer.class);
    private Consumer mockTrackIDObserver = mock(Consumer.class);
    private Consumer mockTrackerStateObserver = mock(Consumer.class);

    @BeforeClass
    public static void beforeClass() throws IOException {
        //may fail sporadically if the app is not installed - just re-run
        revokePermission();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        revokePermission();
    }

    @Before
    public void before() throws Exception {
        WTApplication application = ApplicationProvider.getApplicationContext();
        TestComponent component = (TestComponent) application.getAppComponent();
        component.inject(this);

        disposable = new CompositeDisposable();
        disposable.add(PermissionRequestObservable
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mockPermissinObserver));
        disposable.add(trackID
                .subject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mockTrackIDObserver));
        disposable.add(SensorGPS
                .subjectTrackingState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mockTrackerStateObserver));
        Admin.cleanDb();
        Admin.randReset();

        reset(trackID);
        reset(mockErrorObserver);
        reset(mockPermissinObserver);
        reset(mockTrackIDObserver);
    }

    @After
    public void after() {
        disposable.clear();
    }


    @Test
    public void activity_firstLaunchTrackingOff() {
        doReturn(false).when(isTracking).isOn();
        doReturn(true).when(isTracking).isOff();
        doReturn("xxx").when(trackID).get();
        doReturn(true).when(trackID).isSet();
        doReturn(false).when(trackID).isNotSet();
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {

            assertThat(isTracking.isOn(), equalTo(false));
            assertThat(isTracking.isOff(), equalTo(true));
            assertThat(trackID.get(), equalTo("xxx"));
            assertThat(trackID.isNotSet(), equalTo(false));
            assertThat(trackID.isSet(), equalTo(true));

            onView(withId(R.id.switch_on))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.switch_off))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void activity_firstLaunchTrackingOn() throws Exception {
        doReturn(true).when(isTracking).isOn();
        doReturn(false).when(isTracking).isOff();
        doReturn("xxx").when(trackID).get();
        doReturn(true).when(trackID).isSet();
        doReturn(false).when(trackID).isNotSet();
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {

            assertThat(isTracking.isOn(), equalTo(true));
            assertThat(isTracking.isOff(), equalTo(false));
            assertThat(trackID.get(), equalTo("xxx"));
            assertThat(trackID.isNotSet(), equalTo(false));
            assertThat(trackID.isSet(), equalTo(true));

            verify(mockPermissinObserver, timeout(5000)).accept(any());

            Permission.allowPermissions();
            onView(withId(R.id.switch_off))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.switch_on))
                    .check(matches(not(isDisplayed())));

            ArgumentCaptor<SensorGPS.TrackingState> arg = ArgumentCaptor.forClass(SensorGPS.TrackingState.class);
            verify(mockTrackerStateObserver, timeout(100000).atLeast(1)).accept(arg.capture());

            List<SensorGPS.TrackingState> states = arg.getAllValues();
            assertThat(SensorGPS.isSuspended, is(false));

            Thread.sleep(1000); // let animation to end
        }
    }
}
