package uz.uzinfocom.app.platform.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Configuration
public class I18nConfig {

    private static final Locale UZ_LATIN = Locale.forLanguageTag("uz");
    private static final Locale UZ_CYRILLIC = Locale.forLanguageTag("uz-Cyrl");
    private static final Locale KARAKALPAK = Locale.forLanguageTag("kaa");
    private static final Locale RUSSIAN = Locale.forLanguageTag("ru");

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source =
                new ResourceBundleMessageSource();

        source.setBasename("i18n/messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);

        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(UZ_LATIN);
        resolver.setSupportedLocales(List.of(
                UZ_LATIN,
                UZ_CYRILLIC,
                KARAKALPAK,
                RUSSIAN,
                Locale.ENGLISH
        ));

        return resolver;
    }
}