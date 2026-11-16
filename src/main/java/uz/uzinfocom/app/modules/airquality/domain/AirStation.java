package uz.uzinfocom.app.modules.airquality.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * An air-quality monitoring station of the AIR_CONTROL system (Gidrometeorologiya
 * agentligi). {@link #externalId} is AIR_CONTROL's own station id and the upsert key.
 * {@link #regionCode}/{@link #districtCode} are resolved from {@link #soatoId}
 * against {@code ref_region}/{@code ref_district} (plain columns, no FK); both stay
 * {@code null} when the SOATO code matches nothing here.
 */
@Getter
@Setter
@Entity
@Table(
        name = "air_station",
        indexes = {
                @Index(name = "idx_air_station_region_code", columnList = "region_code"),
                @Index(name = "idx_air_station_district_code", columnList = "district_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_air_station_external_id", columnNames = "external_id")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AirStation extends AuditableEntity {

    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;

    @Column(name = "name")
    private String name;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "soato_id")
    private Integer soatoId;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @Column(name = "district_code", length = 50)
    private String districtCode;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** Wall-clock time of the newest stored measurement; {@code null} until the first one arrives. */
    @Column(name = "last_measured_at")
    private LocalDateTime lastMeasuredAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}
