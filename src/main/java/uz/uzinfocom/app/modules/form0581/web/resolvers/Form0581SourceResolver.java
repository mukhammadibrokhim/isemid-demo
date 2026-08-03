package uz.uzinfocom.app.modules.form0581.web.resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581SourceHeaderTooLongException;
import uz.uzinfocom.app.modules.form0581.application.exception.InvalidForm0581SourceHeaderException;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Form0581SourceResolver {

    private static final String DEFAULT_SOURCE_KEY = "form0581.default-source";
    private static final String ALLOWED_SOURCES_KEY = "form0581.allowed-sources";

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
            throw new Form0581SourceHeaderTooLongException(MAX_SOURCE_LENGTH);
        }

        List<String> allowedSources = systemSettingResolver.resolveStringList(ALLOWED_SOURCES_KEY, ALLOWED_SOURCES);
        Set<String> allowedSourceSet = Set.copyOf(allowedSources);

        if (!allowedSourceSet.contains(source)) {
            throw new InvalidForm0581SourceHeaderException(source, String.join(", ", allowedSources));
        }

        return source;
    }
}
