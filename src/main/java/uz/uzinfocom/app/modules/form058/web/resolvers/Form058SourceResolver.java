package uz.uzinfocom.app.modules.form058.web.resolvers;

import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

@Component
public class Form058SourceResolver {

    private static final String DEFAULT_SOURCE = "LOCAL";

    private static final Set<String> ALLOWED_SOURCES = Set.of(
            "QR",
            "LOCAL",
            "DMED",
            "HEPATITIS"
    );

    @SneakyThrows
    public String resolve(String sourceHeader) {
        if (!StringUtils.hasText(sourceHeader)) {
            return DEFAULT_SOURCE;
        }

        String source = sourceHeader.trim().toUpperCase(Locale.ROOT);

        if (source.length() > 64) {
            throw new BadRequestException("X-Source header is too long");
        }

        if (!ALLOWED_SOURCES.contains(source)) {
            throw new BadRequestException("Invalid X-Source header: " + source);
        }

        return source;
    }
}