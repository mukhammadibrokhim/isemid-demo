package uz.uzinfocom.app.modules.form129.web.resolvers;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ValidationException;

import java.util.Locale;

@Component
public class Form129SourceResolver {

    private static final String DEFAULT_SOURCE = "MANUAL";
    private static final int MAX_SOURCE_LENGTH = 64;

    public String resolve(String sourceHeader) {
        if (!StringUtils.hasText(sourceHeader)) {
            return DEFAULT_SOURCE;
        }

        String source = sourceHeader.trim().toUpperCase(Locale.ROOT);

        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new Form129ValidationException("error.form129.source-header-too-long", MAX_SOURCE_LENGTH);
        }

        return source;
    }
}
