package solutions.s4y.waytoday.errors;

import io.reactivex.subjects.PublishSubject;
import solutions.s4y.waytoday.BuildConfig;

public class ErrorsObservable {
    static final public PublishSubject<ErrorNotification> subject = PublishSubject.create();

    public static void notify(ErrorNotification errorNotification) {
        subject.onNext(errorNotification);
    }

    public static void notify(Throwable throwable) {
        subject.onNext(new ErrorNotification(throwable));
    }

    public static void notify(Throwable throwable, boolean toast) {
        subject.onNext(new ErrorNotification(throwable, toast));
    }

    public static void notifyDev(Throwable throwable) {
        subject.onNext(new ErrorNotification(throwable, BuildConfig.DEBUG));
    }

    public static void toast(Throwable throwable) {
        notify(throwable, true);
    }

}