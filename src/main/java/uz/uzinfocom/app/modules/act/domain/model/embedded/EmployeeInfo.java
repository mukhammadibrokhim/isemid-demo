package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInfo {

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "position_uz")
    private String positionUz;

    @Column(name = "position_ru")
    private String positionRu;

    /**
     * Which identifier ({@link CitizenLookupType}) was used to resolve this
     * person's data from the citizen registry — set for a facility
     * {@code participant}, who isn't a system account and so can't be
     * resolved any other way. Left {@code null} for a {@code sampler},
     * whose info comes from their own logged-in account instead.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", length = 20)
    private CitizenLookupType identifierType;

    @Column(name = "identifier_value")
    private String identifierValue;
}
