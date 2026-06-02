package uz.uzinfocom.app.platform.security.authorization.permission;

public enum PermissionAction {

    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    MANAGE("manage");

    private final String code;

    PermissionAction(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}