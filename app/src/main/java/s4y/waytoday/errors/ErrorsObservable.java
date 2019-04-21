package s4y.waytoday.errors;

import io.reactivex.subjects.PublishSubject;

public class ErrorsObservable {
    static final public PublishSubject<ErrorNotification> subject = PublishSubject.create();

    private static void notify(ErrorNotification errorNotification) {
        subject.onNext(errorNotification);
    }

    public static void notify(Throwable throwable) {
        notify(new ErrorNotification(throwable));
    }

    private static void notify(int resourceID) {
        notify(new ErrorNotification(resourceID, true));
    }

    public static void notify(Throwable throwable, boolean toast) {
        notify(new ErrorNotification(throwable, toast));
    }

    public static void notify(String message, boolean toast) {
        notify(new ErrorNotification(message, toast));
    }

    public static void toast(Throwable throwable) {
        notify(throwable, true);
    }

    public static void toast(int resourceID) {
        notify(resourceID);
    }

}