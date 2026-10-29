package uz.uzinfocom.app.platform.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessageSource messageSource;

    public String resolve(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }

    /**
     * For code running in the servlet-filter phase (Spring Security handlers,
     * {@code ErrorResponseFilter}), where {@link LocaleContextHolder} holds the
     * servlet container's {@code request.getLocale()} rather than the locale
     * {@code LenientAcceptLanguageResolver} derives from {@code Accept-Language}.
     */
    public String resolveFor(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }
}
