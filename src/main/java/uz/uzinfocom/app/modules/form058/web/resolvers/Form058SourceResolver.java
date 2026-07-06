package uz.uzinfocom.app.modules.form058.web.resolvers;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.exception.Form058SourceHeaderTooLongException;
import uz.uzinfocom.app.modules.form058.application.exception.InvalidForm058SourceHeaderException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class Form058SourceResolver {

    private static final String DEFAULT_SOURCE = "DMED";
    private static final int MAX_SOURCE_LENGTH = 64;

    private static final List<String> ALLOWED_SOURCES = List.of(
            "QR",
            "MANUAL",
            "DMED",
            "HEPATITIS"
    );

    private static final Set<String> ALLOWED_SOURCE_SET = Set.copyOf(ALLOWED_SOURCES);

    private static final String ALLOWED_SOURCES_TEXT = String.join(", ", ALLOWED_SOURCES);

    public String resolve(String sourceHeader) {
        if (!StringUtils.hasText(sourceHeader)) {
            return DEFAULT_SOURCE;
        }

        String source = sourceHeader.trim().toUpperCase(Locale.ROOT);

        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new Form058SourceHeaderTooLongException(MAX_SOURCE_LENGTH);
        }

        if (!ALLOWED_SOURCE_SET.contains(source)) {
            throw new InvalidForm058SourceHeaderException(source, ALLOWED_SOURCES_TEXT);
        }

        return source;
    }
}