package solutions.s4y.waytoday.errors;

import org.junit.Test;

import io.reactivex.disposables.Disposable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ErrorNotifierTest {

    @Test
    public void errorObserver_shouldRecieveNotification() {
        Runnable run = mock(Runnable.class);
        verify(run, never()).run();

        Exception e = new Exception();
        ErrorNotifier.notify(e);
        verify(run, never()).run();

        Disposable d = ErrorNotifier
                .subject
                .subscribe(err -> {
                    run.run();
                });

        ErrorNotifier.notify(e);
        verify(run, times(1)).run();

        d.dispose();

        ErrorNotifier.notify(e);
        verify(run, times(1)).run();
    }
}
