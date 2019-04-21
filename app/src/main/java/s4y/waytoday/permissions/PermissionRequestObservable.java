package s4y.waytoday.permissions;

import io.reactivex.subjects.PublishSubject;

public class PermissionRequestObservable {
    static public final PublishSubject<PermissionRequest> subject = PublishSubject.create();

    static public void onNext(String permission, RestartOnGivenPermssion restarter) {
        subject.onNext(new PermissionRequest(permission, restarter));
    }
}
