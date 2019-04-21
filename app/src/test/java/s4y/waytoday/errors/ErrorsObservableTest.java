package s4y.waytoday.errors;

import org.junit.Test;

import io.reactivex.disposables.Disposable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ErrorsObservableTest {

    @Test
    public void errorObserver_shouldRecieveNotification() {
        Runnable run = mock(Runnable.class);
        verify(run, never()).run();

        Exception e = new Exception();
        ErrorsObservable.notify(e);
        verify(run, never()).run();

        Disposable d = ErrorsObservable
                .subject
                .subscribe(err -> {
                    run.run();
                });

        ErrorsObservable.notify(e);
        verify(run, times(1)).run();

        d.dispose();

        ErrorsObservable.notify(e);
        verify(run, times(1)).run();
    }
}
