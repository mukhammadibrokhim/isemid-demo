package uz.uzinfocom.app.platform.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.core.JsonGenerator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Jackson's built-in {@code Instant} serializer always writes UTC ("...Z"),
 * ignoring {@code spring.jackson.time-zone} — that property only affects
 * {@code Date}/{@code Calendar} formatting, not {@code java.time.Instant}
 * (which has no zone concept to begin with). Left alone, every {@code Instant}
 * field returned by the API (createdAt/updatedAt, occurredAt, etc.) would come
 * back as raw UTC instead of Tashkent time. This overrides the serializer to
 * render Instants with a fixed {@code +05:00} offset instead.
 */
@Configuration
public class JacksonTimeZoneConfig {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Tashkent");

    @Bean
    public JsonMapperBuilderCustomizer instantTimeZoneCustomizer() {
        return (JsonMapper.Builder builder) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Instant.class, new ValueSerializer<Instant>() {
                @Override
                public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) {
                    gen.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value.atZone(APP_ZONE)));
                }
            });
            builder.addModule(module);
        };
    }
}
