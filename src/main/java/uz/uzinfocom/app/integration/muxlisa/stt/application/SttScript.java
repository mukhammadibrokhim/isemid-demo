package uz.uzinfocom.app.integration.muxlisa.stt.application;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Alphabet of the speech-to-text answer, derived from {@code Accept-Language}.
 * Only {@code uz-Cyrl} switches to Cyrillic; {@code uz}, {@code ru}, {@code kaa}
 * (alphabet still to be agreed), unknown, malformed or missing all keep Muxlisa's text as is.
 */
enum SttScript {
    AS_RECEIVED,
    UZBEK_CYRILLIC;

    static SttScript fromAcceptLanguage(String header) {
        if (header == null || header.isBlank()) {
            return AS_RECEIVED;
        }
        List<Locale.LanguageRange> ranges;
        try {
            ranges = Locale.LanguageRange.parse(header);
        } catch (IllegalArgumentException exception) {
            return AS_RECEIVED;
        }
        // Highest weight wins; ties keep the order the client sent.
        return ranges.stream()
                .filter(range -> range.getWeight() > 0)
                .max(Comparator.comparingDouble(Locale.LanguageRange::getWeight))
                .map(SttScript::fromRange)
                .orElse(AS_RECEIVED);
    }

    private static SttScript fromRange(Locale.LanguageRange range) {
        String tag = range.getRange().toLowerCase(Locale.ROOT);
        boolean uzbek = tag.equals("uz") || tag.startsWith("uz-");
        return uzbek && tag.contains("cyrl") ? UZBEK_CYRILLIC : AS_RECEIVED;
    }
}
