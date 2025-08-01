package uz.uzinfocom.isemid.features.form058.dtos.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058UpdateReceivedOrg {
//    private Long workPlaceOrgId;
//    private String workPlaceStateCode;
//    private String workPlaceCityCode;

    private Long receivedOrgId;

    //address
    private String addressStateCode;
    private String addressCityCode;
    private String addressNeighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
