package uz.uzinfocom.app.platform.security.authorization.permission;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    String resource();

    PermissionAction action();
}