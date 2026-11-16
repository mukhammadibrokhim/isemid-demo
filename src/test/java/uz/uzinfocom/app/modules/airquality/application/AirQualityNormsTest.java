package uz.uzinfocom.app.modules.airquality.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirQualityNormsTest {

    @Mock
    private SystemSettingResolver settings;

    private AirQualityNorms norms;

    @BeforeEach
    void setUp() {
        // No row in system_settings: the resolver hands back the code default it was given.
        lenient().when(settings.resolveString(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        norms = new AirQualityNorms(settings);
    }

    @Test
    void usesTheWhoDefaultsWhenNothingIsConfigured() {
        assertThat(norms.limit("PM2.5")).contains(new BigDecimal("15"));
        assertThat(norms.limit("pm 10")).contains(new BigDecimal("45"));
        assertThat(norms.limit("CO")).contains(new BigDecimal("4000"));
    }

    @Test
    void aPollutantWithoutANormIsNotJudged() {
        assertThat(norms.limit("NH3")).isEmpty();
        assertThat(norms.status("NH3", new BigDecimal("500"), "µg/m³")).isEqualTo(AirStatus.NO_DATA);
    }

    @Test
    void aConfiguredNormOverridesTheDefault() {
        when(settings.resolveString(eq("air-quality.norm.pm2.5"), anyString())).thenReturn("35");

        assertThat(norms.limit("PM2.5")).contains(new BigDecimal("35"));
    }

    @Test
    void anUnparseableOrNonPositiveSettingFallsBackToTheDefault() {
        when(settings.resolveString(eq("air-quality.norm.pm2.5"), anyString())).thenReturn("fifteen");
        when(settings.resolveString(eq("air-quality.norm.pm10"), anyString())).thenReturn("0");

        assertThat(norms.limit("PM2.5")).contains(new BigDecimal("15"));
        assertThat(norms.limit("PM10")).contains(new BigDecimal("45"));
    }

    @Test
    void convertsUnitsToMicrogramsAndRefusesTheRest() {
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), "µg/m³")).contains(new BigDecimal("10"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), "ug/m3")).contains(new BigDecimal("10"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), "μg / m³")).contains(new BigDecimal("10"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("4"), "mg/m³")).contains(new BigDecimal("4000"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("2500"), "ng/m3")).hasValueSatisfying(
                value -> assertThat(value).isEqualByComparingTo("2.5"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), null)).contains(new BigDecimal("10"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), " ")).contains(new BigDecimal("10"));
        assertThat(AirQualityNorms.toMicrograms(new BigDecimal("10"), "ppm")).isEqualTo(Optional.empty());
    }

    @Test
    void ratioAndStatusFollowTheNorm() {
        // PM2.5 limit 15 µg/m³
        assertThat(norms.status("PM2.5", new BigDecimal("7"), "µg/m³")).isEqualTo(AirStatus.GOOD);
        assertThat(norms.status("PM2.5", new BigDecimal("15"), "µg/m³")).isEqualTo(AirStatus.MODERATE);
        assertThat(norms.status("PM2.5", new BigDecimal("16"), "µg/m³")).isEqualTo(AirStatus.EXCEEDED);
        assertThat(norms.status("PM2.5", new BigDecimal("30"), "µg/m³")).isEqualTo(AirStatus.EXCEEDED);
        assertThat(norms.status("PM2.5", new BigDecimal("31"), "µg/m³")).isEqualTo(AirStatus.SEVERE);
        // CO reported in mg/m³: 5 mg/m³ = 5000 µg/m³ against 4000
        assertThat(norms.ratio("CO", new BigDecimal("5"), "mg/m³")).hasValueSatisfying(
                ratio -> assertThat(ratio).isEqualByComparingTo("1.25"));
        assertThat(norms.status("PM2.5", new BigDecimal("500"), "ppb")).isEqualTo(AirStatus.NO_DATA);
    }
}
