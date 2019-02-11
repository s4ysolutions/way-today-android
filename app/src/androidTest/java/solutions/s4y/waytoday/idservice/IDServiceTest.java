package solutions.s4y.waytoday.idservice;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import solutions.s4y.waytoday.TestComponent;
import solutions.s4y.waytoday.WTApplication;
import solutions.s4y.waytoday.errors.ErrorNotification;
import solutions.s4y.waytoday.errors.ErrorsObservable;
import solutions.s4y.waytoday.preferences.PreferenceGRPCHost;
import solutions.s4y.waytoday.preferences.PreferenceGRPCPort;
import solutions.s4y.waytoday.preferences.PreferenceIsTracking;
import solutions.s4y.waytoday.preferences.PreferenceTrackID;
import solutions.s4y.waytoday.utils.Admin;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IDServiceTest {

    @Inject
    PreferenceIsTracking isTracking;
    @Inject
    PreferenceTrackID trackID;
    @Inject
    PreferenceGRPCHost grpcHost;
    @Inject
    PreferenceGRPCPort grpcPort;

    private CompositeDisposable disposable;
    private Consumer mockErrorObserver = mock(Consumer.class);

    @BeforeClass
    public static void beforeClass() {
        IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void AfterClass() {
    }

    @Before
    public void setUp() throws Exception {
        disposable = new CompositeDisposable();
        Admin.cleanDb();
        Admin.randReset();

        WTApplication application = ApplicationProvider.getApplicationContext();
        TestComponent component = (TestComponent) application.getAppComponent();
        component.inject(this);

        reset(trackID);
        reset(grpcHost);
        reset(grpcPort);
        reset(mockErrorObserver);

        when(grpcHost.get()).thenReturn(Admin.TEST_HOST);
        when(grpcPort.get()).thenReturn(9001);
    }

    @After
    public void TearDown() {
        disposable.clear();
    }

    @Test
    public void idService_shouldFailOnWrongHostPort() throws Exception {
        trackID.set("");
        reset(trackID);
        reset(grpcHost);
        reset(grpcPort);
        when(grpcHost.get()).thenReturn("xx");
        when(grpcPort.get()).thenReturn(31459);

        //noinspection unchecked
        disposable.add(ErrorsObservable.subject.subscribe(mockErrorObserver));

        WTApplication application = ApplicationProvider.getApplicationContext();

        IDService.enqueueRetrieveId(application, "");
        //noinspection unchecked
        verify(mockErrorObserver, timeout(5000).atLeastOnce()).accept(any(ErrorNotification.class));
        verify(trackID, never()).set(any());
        assertThat(trackID.get()).isEqualTo("");
    }

    @Test
    public void idService_shouldPassOnTestHostWithEmptyPrevID() throws Exception {
        when(isTracking.isOn()).thenReturn(false);
        when(trackID.get()).thenReturn("");

        //noinspection unchecked
        disposable.add(ErrorsObservable.subject.subscribe(mockErrorObserver));

        WTApplication application = ApplicationProvider.getApplicationContext();

        IDService.enqueueRetrieveId(application, "");
        verify(trackID, timeout(5000)).set(Admin.FIRST_RAND_ID);
        //noinspection unchecked
        verify(mockErrorObserver, never()).accept(any(ErrorNotification.class));
    }

    @Test
    public void idService_shouldPassOnTestHostWithExistingPrevID() throws Exception {
        when(isTracking.isOn()).thenReturn(false);

        //noinspection unchecked
        disposable.add(ErrorsObservable.subject.subscribe(mockErrorObserver));

        WTApplication application = ApplicationProvider.getApplicationContext();

        IDService.enqueueRetrieveId(application, "");
        verify(trackID, timeout(5000)).set(Admin.FIRST_RAND_ID);
        assertThat(trackID.get()).isEqualTo(Admin.FIRST_RAND_ID);
        //noinspection unchecked
        verify(mockErrorObserver, never()).accept(any(ErrorNotification.class));
    }
}
