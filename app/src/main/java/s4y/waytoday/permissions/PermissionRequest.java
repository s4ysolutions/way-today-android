package s4y.waytoday.permissions;

public class PermissionRequest {
    public final RestartOnGivenPermssion restarter;
    public final String permission;

    PermissionRequest(String permissions, RestartOnGivenPermssion restarter) {
        this.permission = permissions;
        this.restarter = restarter;
    }
}
