package uz.uzinfocom.app.modules.card.domain.model.card161;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

/**
 * Legacy fix: the {@code card161_id} FK had no unique constraint even though
 * the relationship is conceptually 1:1, so nothing at the DB level actually
 * prevented two InfectionSourceDetail rows from pointing at the same
 * Card161. This side now owns the FK with an explicit unique constraint.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card161_infection_source_detail",
        uniqueConstraints = @UniqueConstraint(name = "uk_card161_infection_source_detail", columnNames = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InfectionSourceDetail extends UuidAuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_infection_source_detail_card161"))
    private Card161 card161;

    @CatalogCode("card161-infection-source-not-found-reason")
    @Column(name = "infection_source_not_found_code", length = 64)
    private String infectionSourceNotFoundCode;

    @Column(name = "person_full_name", length = 500)
    private String personFullName;

    @CatalogCode("card161-infection-source-disease-period")
    @Column(name = "infection_source_disease_period_code", length = 64)
    private String infectionSourceDiseasePeriodCode;

    @CatalogCode("card161-animal-type")
    @Column(name = "animal_type_code", length = 64)
    private String animalTypeCode;
}
