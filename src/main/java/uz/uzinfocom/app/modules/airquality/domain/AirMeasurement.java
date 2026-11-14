package uz.uzinfocom.app.modules.airquality.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One pollutant value of one station at one moment: {@code (stationId, measuredAt,
 * parameter)} is unique. {@link #parameter} is the normalized code (see
 * {@code AirParameter}), e.g. {@code PM2.5}, {@code NO2}. {@link #measuredAt} is the
 * station's local wall clock exactly as AIRCONTROL sends it (no zone).
 */
@Getter
@Setter
@Entity
@Table(
        name = "air_measurement",
        indexes = {
                @Index(name = "idx_air_measurement_measured_at", columnList = "measured_at"),
                @Index(name = "idx_air_measurement_parameter_time", columnList = "parameter, measured_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_air_measurement_station_time_param",
                        columnNames = {"station_id", "measured_at", "parameter"})
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AirMeasurement extends AuditableEntity {

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "parameter", nullable = false, length = 30)
    private String parameter;

    @Column(name = "value", nullable = false, precision = 18, scale = 6)
    private BigDecimal value;

    @Column(name = "unit", length = 30)
    private String unit;
}
