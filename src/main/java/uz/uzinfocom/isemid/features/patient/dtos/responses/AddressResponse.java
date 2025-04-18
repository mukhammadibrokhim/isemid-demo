package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private AddressType type;
    private CatalogRespDto state;
    private CatalogRespDto city;
    private CatalogRespDto neighborhood;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
