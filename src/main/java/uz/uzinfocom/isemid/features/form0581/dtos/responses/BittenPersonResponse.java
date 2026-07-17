package uz.uzinfocom.isemid.features.form0581.dtos.responses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BittenPersonResponse {
    private Long id;
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
