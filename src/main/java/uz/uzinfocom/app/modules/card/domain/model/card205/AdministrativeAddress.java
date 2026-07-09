package uz.uzinfocom.app.modules.card.domain.model.card205;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Region/district/neighborhood/street/house/apartment breakdown — the exact
 * same six fields were duplicated verbatim across
 * {@link InformationOtherBittenPeople} and
 * {@link InformationAboutAnimaBittenPeople}. Scoped to card205 rather than a
 * shared package since those are its only two users today; promote it if a
 * third card type ever needs the same shape.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAddress {

    @Column(name = "region", length = 64)
    private String region;

    @Column(name = "district", length = 64)
    private String district;

    @Column(name = "neighborhood", length = 255)
    private String neighborhood;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "house_number", length = 32)
    private String houseNumber;

    @Column(name = "apartment_number", length = 32)
    private String apartmentNumber;
}
