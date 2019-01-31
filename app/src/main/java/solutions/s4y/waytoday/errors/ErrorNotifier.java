package solutions.s4y.waytoday.errors;

import io.reactivex.subjects.PublishSubject;

public class ErrorNotifier {
    static final public PublishSubject<ErrorNotification> subject = PublishSubject.create();

    public static void notify(ErrorNotification errorNotification) {
        subject.onNext(errorNotification);
    }

    public static void notify(Throwable throwable) {
        subject.onNext(new ErrorNotification(throwable));
    }
}