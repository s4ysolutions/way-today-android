package solutions.s4y.waytoday.permissions;

public class PermissionRequest {
    private final RestartOnGivenPermssion restarter;
    private final String[] permissions;

    public PermissionRequest(String[] permissions, RestartOnGivenPermssion restarter) {
        this.permissions = permissions;
        this.restarter = restarter;
    }
}
