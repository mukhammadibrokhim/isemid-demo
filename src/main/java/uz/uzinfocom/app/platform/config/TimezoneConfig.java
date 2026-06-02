package uz.uzinfocom.app.platform.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    @PostConstruct
    void initializeDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
    }
}
