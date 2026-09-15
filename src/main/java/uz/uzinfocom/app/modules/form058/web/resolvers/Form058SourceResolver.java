package uz.uzinfocom.app.modules.form058.web.resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.exception.Form058SourceHeaderTooLongException;
import uz.uzinfocom.app.modules.form058.application.exception.InvalidForm058SourceHeaderException;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Form058SourceResolver {

    private static final String DEFAULT_SOURCE_KEY = "form058.default-source";
    private static final String ALLOWED_SOURCES_KEY = "form058.allowed-sources";

    private static final String DEFAULT_SOURCE = "MANUAL";
    private static final int MAX_SOURCE_LENGTH = 64;

    private static final List<String> ALLOWED_SOURCES = List.of(
            "MANUAL"
    );

    private final SystemSettingResolver systemSettingResolver;

    public String resolve(String sourceHeader) {
        String defaultSource = systemSettingResolver.resolveString(DEFAULT_SOURCE_KEY, DEFAULT_SOURCE);

        if (!StringUtils.hasText(sourceHeader)) {
            return defaultSource;
        }

        String source = sourceHeader.trim().toUpperCase(Locale.ROOT);

        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new Form058SourceHeaderTooLongException(MAX_SOURCE_LENGTH);
        }

        List<String> allowedSources = systemSettingResolver.resolveStringList(ALLOWED_SOURCES_KEY, ALLOWED_SOURCES);
        Set<String> allowedSourceSet = Set.copyOf(allowedSources);

        if (!allowedSourceSet.contains(source)) {
            throw new InvalidForm058SourceHeaderException(source, String.join(", ", allowedSources));
        }

        return source;
    }
}
