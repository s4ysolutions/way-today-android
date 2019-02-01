package solutions.s4y.waytoday.permissions;

import io.reactivex.subjects.PublishSubject;

public class PermissionRequestObservable {
    static public final PublishSubject<PermissionRequest> subject = PublishSubject.create();

    static public void onNext(String permission, RestartOnGivenPermssion restarter) {
        subject.onNext(new PermissionRequest(new String[]{permission}, restarter));
    }

    static public void onNext(String permission1, String permission2, RestartOnGivenPermssion restarter) {
        subject.onNext(new PermissionRequest(new String[]{permission1, permission2}, restarter));
    }
}
