package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581AnimalOwnerInfo {

    @Column(name = "owner_last_name", length = 255)
    private String ownerLastName;

    @Column(name = "owner_first_name", length = 255)
    private String ownerFirstName;

    @Column(name = "owner_middle_name", length = 255)
    private String ownerMiddleName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "regionCode", column = @Column(name = "owner_region_code")),
            @AttributeOverride(name = "districtCode", column = @Column(name = "owner_district_code")),
            @AttributeOverride(name = "neighborhoodCode", column = @Column(name = "owner_neighborhood_code")),
            @AttributeOverride(name = "street", column = @Column(name = "owner_street")),
            @AttributeOverride(name = "houseNumber", column = @Column(name = "owner_house_number")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "owner_apartment_number"))
    })
    private Form0581Address ownerAddress;
}
