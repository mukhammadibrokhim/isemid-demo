package uz.uzinfocom.app.modules.card.domain.model.card161;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A vaccination record belonging to a single Card161 — not a shared,
 * cross-module vaccination concept (the legacy module modeled it as a plain
 * owned child, despite living in a "commons" package there).
 */
@Getter
@Setter
@Entity
@Table(
        name = "card161_vaccination",
        indexes = @Index(name = "idx_card161_vaccination_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Vaccination extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_vaccination_card161"))
    private Card161 card161;

    @CatalogCode("card161-vaccination-verified")
    @Column(name = "vaccination_verified_code", length = 64)
    private String vaccinationVerifiedCode;

    @Column(name = "vaccination_name", length = 255)
    private String vaccinationName;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "vaccination_date")
    private LocalDateTime vaccinationDate;

    @Column(name = "dose_volume", precision = 10, scale = 3)
    private BigDecimal doseVolume;

    @Column(name = "scheduled")
    private Boolean scheduled;

    // The fields below mirror DHP FHIR Immunization (integration.dhp.immunization),
    // so a record prefilled from DHP is stored without loss. fhirId is null for
    // rows entered by hand.

    @Column(name = "fhir_id", length = 64)
    private String fhirId;

    @Column(name = "vaccine_code", length = 64)
    private String vaccineCode;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "dose_unit", length = 32)
    private String doseUnit;

    @Column(name = "dose_number")
    private Integer doseNumber;

    /** Joined with {@code "; "} — see {@code Card161Mapper}. */
    @Column(name = "target_diseases", length = 500)
    private String targetDiseases;

    @Column(name = "performer_name", length = 255)
    private String performerName;
}
