package uz.uzinfocom.app.platform.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Accept-Language resolver that understands the spellings real clients send for
 * the app's locales — {@code uz_Cyrl}, {@code uz-cyrl}, {@code uz-Cyrillic},
 * {@code oz} for Uzbek Cyrillic; {@code uz-Latn}, {@code uz-UZ} for Latin — which
 * the stock AcceptHeaderLocaleResolver would silently treat as plain Latin
 * {@code uz}. Entries are tried in q-value order; no match gives the default.
 */
public class LenientAcceptLanguageResolver implements LocaleResolver {

    static final Locale UZ_LATIN = Locale.forLanguageTag("uz");
    static final Locale UZ_CYRILLIC = Locale.forLanguageTag("uz-Cyrl");
    static final Locale KARAKALPAK = Locale.forLanguageTag("kaa");
    static final Locale RUSSIAN = Locale.forLanguageTag("ru");

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        return parse(header).orElse(UZ_LATIN);
    }

    static Optional<Locale> parse(@Nullable String header) {
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .sorted(Comparator.comparingDouble(LenientAcceptLanguageResolver::quality).reversed())
                .map(part -> normalize(part.split(";")[0]))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static double quality(String part) {
        for (String param : part.split(";")) {
            String p = param.trim();
            if (p.startsWith("q=")) {
                try {
                    return Double.parseDouble(p.substring(2));
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 1.0;
    }

    private static Optional<Locale> normalize(String rawTag) {
        String tag = rawTag.trim().toLowerCase(Locale.ROOT).replace('_', '-');

        if (tag.equals("oz") || tag.startsWith("oz-") || (tag.startsWith("uz") && tag.contains("cyr"))) {
            return Optional.of(UZ_CYRILLIC);
        }
        if (tag.equals("uz") || tag.startsWith("uz-")) {
            return Optional.of(UZ_LATIN);
        }
        if (tag.equals("kaa") || tag.startsWith("kaa-")) {
            return Optional.of(KARAKALPAK);
        }
        if (tag.equals("ru") || tag.startsWith("ru-")) {
            return Optional.of(RUSSIAN);
        }
        if (tag.equals("en") || tag.startsWith("en-")) {
            return Optional.of(Locale.ENGLISH);
        }
        return Optional.empty();
    }

    @Override
    public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
        throw new UnsupportedOperationException("Locale is taken from the Accept-Language header only");
    }
}
