package uz.uzinfocom.app.platform.iam.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.domain.enums.ServiceType;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "organization")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends AuditableEntity {

    @Column(nullable = false)
    private UUID uuid;

    @Column(length = 50)
    private String tin;

    @Column(nullable = false, length = 500)
    private String name;

    private Boolean active;

    @Column(length = 50)
    private String phone;

    @Column(name = "state_code", length = 64)
    private String regionCode;

    @Column(name = "city_code", length = 64)
    private String districtCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "level_type", nullable = false, length = 50)
    private OrganizationLevel levelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_type", nullable = false, length = 50)
    private MedicalType medicalType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "organization_service_types", joinColumns = @JoinColumn(name = "organization_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private List<ServiceType> serviceTypes = new ArrayList<>();

}
