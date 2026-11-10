package uz.uzinfocom.app.modules.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationSource;

/**
 * "Doimiy aholi soni" — one permanent-population figure for one SOATO
 * territory in one calendar year. Sourced from the stat.uz SDMX feed
 * (dataset 246), keyed by {@code (soatoId, year)}. {@link #population} is the
 * absolute head-count (the feed's "thousand people" value multiplied by
 * 1000), so per-capita report rates can divide with integer arithmetic.
 * <p>
 * {@link #regionCode}/{@link #districtCode} are the resolved {@code
 * ref_region}/{@code ref_district} codes for this {@link #soatoId} — plain
 * columns, no FK, matching how the rest of {@code modules.reference} links
 * geography. They are {@code null} for {@link PopulationGeoType#REPUBLIC} and
 * for {@link PopulationGeoType#OTHER} (a SOATO territory the feed carries
 * that has no active match here yet). The territory's display name is never
 * stored — it is resolved by code/soatoId against {@code
 * ReferenceLookupService} at read time.
 * <p>
 * Unlike its sibling reference entities this extends {@link AuditableEntity}
 * (not {@code ReferenceDictionaryEntity}) so the super-admin detail view can
 * show who created / last changed the row and when. SDMX-sync writes carry
 * whatever actor ran the sync (or none); the seed carries none.
 */
@Getter
@Setter
@Entity
@Table(
        name = "ref_population",
        indexes = {
                @Index(name = "idx_ref_population_year", columnList = "stat_year"),
                @Index(name = "idx_ref_population_region_code", columnList = "region_code"),
                @Index(name = "idx_ref_population_district_code", columnList = "district_code"),
                @Index(name = "idx_ref_population_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_population_soato_year", columnNames = {"soato_id", "stat_year"})
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Population extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "geo_type", nullable = false, length = 20)
    private PopulationGeoType geoType;

    @Column(name = "soato_id", nullable = false)
    private Integer soatoId;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @Column(name = "district_code", length = 50)
    private String districtCode;

    @Column(name = "stat_year", nullable = false)
    private Integer year;

    @Column(name = "population", nullable = false)
    private Long population;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private PopulationSource source = PopulationSource.SDMX;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
