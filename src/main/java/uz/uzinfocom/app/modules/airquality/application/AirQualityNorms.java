package uz.uzinfocom.app.modules.airquality.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Per-pollutant norms ("me'yor") and the unit handling that makes a reading comparable with
 * one. Everything is compared in micrograms per cubic metre: a reading in {@code mg/m³}
 * is scaled, one in {@code ng/m³} too, a missing unit is taken as {@code µg/m³} (what the
 * agreement's sample uses), and anything else ({@code ppm}, {@code ppb}...) cannot be
 * converted without the gas's molar mass and the station's temperature, so it is
 * reported as {@link AirStatus#NO_DATA} instead of guessed.
 * <p>
 * The defaults are the WHO 2021 24-hour air quality guideline values. They are plain
 * {@code system_settings} ({@code air-quality.norm.<code>}), so the national MPC values can
 * replace them from the dev panel without a deploy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirQualityNorms {

    /** Unit every limit and every converted value is expressed in: {@code µg/m³}. */
    public static final String UNIT = "µg/m³";

    private static final Map<String, BigDecimal> DEFAULT_LIMITS = Map.of(
            "PM2.5", new BigDecimal("15"),
            "PM10", new BigDecimal("45"),
            "NO2", new BigDecimal("25"),
            "SO2", new BigDecimal("40"),
            "O3", new BigDecimal("100"),
            "CO", new BigDecimal("4000")
    );

    private final SystemSettingResolver settings;

    /** The limit of one pollutant in {@link #UNIT}; empty when the pollutant has no norm. */
    public Optional<BigDecimal> limit(String parameterCode) {
        String code = AirParameter.normalize(parameterCode);
        if (code == null) {
            return Optional.empty();
        }
        BigDecimal fallback = DEFAULT_LIMITS.get(code);
        String key = AirQualitySettingKeys.NORM_PREFIX + code.toLowerCase(Locale.ROOT);
        String raw = settings.resolveString(key, fallback == null ? "" : fallback.toPlainString());
        if (!StringUtils.hasText(raw)) {
            return Optional.ofNullable(fallback);
        }
        try {
            BigDecimal parsed = new BigDecimal(raw.trim());
            if (parsed.signum() > 0) {
                return Optional.of(parsed);
            }
        } catch (NumberFormatException exception) {
            log.warn("Air quality norm setting is not a number, using the default. key={}", key);
        }
        return Optional.ofNullable(fallback);
    }

    /** {@code value / limit} (4 decimals); empty when there is no norm or the unit cannot be converted. */
    public Optional<BigDecimal> ratio(String parameterCode, BigDecimal value, String unit) {
        if (value == null) {
            return Optional.empty();
        }
        Optional<BigDecimal> limit = limit(parameterCode);
        Optional<BigDecimal> micrograms = toMicrograms(value, unit);
        if (limit.isEmpty() || micrograms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(micrograms.get().divide(limit.get(), 4, RoundingMode.HALF_UP));
    }

    public AirStatus status(String parameterCode, BigDecimal value, String unit) {
        return AirStatus.fromRatio(ratio(parameterCode, value, unit).orElse(null));
    }

    /** The reading in {@link #UNIT}; empty for a unit that cannot be converted. */
    public static Optional<BigDecimal> toMicrograms(BigDecimal value, String unit) {
        if (value == null) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(unit)) {
            return Optional.of(value);
        }
        String normalized = unit.toLowerCase(Locale.ROOT)
                .replace('³', '3')
                .replace('µ', 'u')
                .replace('μ', 'u')
                .replaceAll("\\s+", "");
        return switch (normalized) {
            case "ug/m3", "mcg/m3" -> Optional.of(value);
            case "mg/m3" -> Optional.of(value.multiply(BigDecimal.valueOf(1000)));
            case "ng/m3" -> Optional.of(value.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP));
            default -> Optional.empty();
        };
    }
}
