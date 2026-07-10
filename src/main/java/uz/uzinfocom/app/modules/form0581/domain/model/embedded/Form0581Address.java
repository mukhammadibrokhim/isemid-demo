package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Region/district/neighborhood/street/house/apartment breakdown — extracted
 * because this exact 6-field shape is needed twice within this module
 * (the bitten animal's owner, and each entry in the repeatable "other
 * injured people" list). Not shared with {@code card205.AdministrativeAddress},
 * which is the same shape but intentionally scoped to its own module.
 */
@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581Address {

    @Column(name = "region_code", length = 64)
    private String regionCode;

    @Column(name = "district_code", length = 64)
    private String districtCode;

    @Column(name = "neighborhood_code", length = 64)
    private String neighborhoodCode;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "house_number", length = 32)
    private String houseNumber;

    @Column(name = "apartment_number", length = 32)
    private String apartmentNumber;
}
