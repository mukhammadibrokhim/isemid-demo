package uz.uzinfocom.app.platform.security.resolver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uz.uzinfocom.app.shared.exception.SecurityException;
import uz.uzinfocom.app.platform.security.annotation.CurrentAuthentication;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;

@Component
@RequiredArgsConstructor
public class CurrentAuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentAuthentication.class)
                && FederatedAuthenticationToken.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof FederatedAuthenticationToken token)) {
            throw new SecurityException("auth.unauthorized");
        }

        return token;
    }
}
