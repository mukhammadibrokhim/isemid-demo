package uz.uzinfocom.isemid.features.form0581.dtos.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalOwnerRequest {
    private String lastName;
    private String firstName;
    private String middleName;

    private String stateCode;
    private String cityCode;
    private String neighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
