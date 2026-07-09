package uz.uzinfocom.app.modules.card.domain.model.card174;

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

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "card174_infection_monitoring",
        indexes = @Index(name = "idx_card174_infection_monitoring_card174_id", columnList = "card174_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InfectionMonitoring extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card174_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card174_infection_monitoring_card174"))
    private Card174 card174;

    @Column(name = "sequential_number")
    private Integer sequentialNumber;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @CatalogCode("gender")
    @Column(name = "gender_code", length = 64)
    private String genderCode;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "profession", length = 255)
    private String profession;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "possible_infection_location", length = 500)
    private String possibleInfectionLocation;

    @Column(name = "possible_infection_factor", length = 500)
    private String possibleInfectionFactor;

    @Column(name = "possible_infection_date")
    private LocalDate possibleInfectionDate;
}
