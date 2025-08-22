package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private AddressType type;
    private CatalogMinResponse state;
    private CatalogMinResponse city;
    private CatalogMinResponse neighborhood;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
