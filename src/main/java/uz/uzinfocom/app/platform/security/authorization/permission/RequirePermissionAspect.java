package uz.uzinfocom.app.platform.security.authorization.permission;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.authorization.AuthorityNames;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class RequirePermissionAspect {

    @Around("@within(uz.uzinfocom.app.platform.security.authorization.permission.RequirePermission) || " +
            "@annotation(uz.uzinfocom.app.platform.security.authorization.permission.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        RequirePermission permission = resolvePermission(joinPoint);

        if (permission == null) {
            return joinPoint.proceed();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());

        String requiredPermission = AuthorityNames.permission(permission.resource(), permission.action().code());
        String managePermission = AuthorityNames.permission(permission.resource(), PermissionAction.MANAGE.code());

        boolean allowed = authorities.contains(requiredPermission)
                || authorities.contains(managePermission);

        if (!allowed) {
            log.warn(
                    "Access denied. requiredPermission={}, managePermission={}, principal={}",
                    requiredPermission,
                    managePermission,
                    authentication.getName()
            );

            throw new AccessDeniedException("Access denied");
        }

        return joinPoint.proceed();
    }

    private RequirePermission resolvePermission(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePermission methodPermission =
                AnnotatedElementUtils.findMergedAnnotation(method, RequirePermission.class);

        if (methodPermission != null) {
            return methodPermission;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();

        return AnnotatedElementUtils.findMergedAnnotation(targetClass, RequirePermission.class);
    }
}
