package uz.uzinfocom.isemid.models.payload.requests.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqForm058OrgUpdate {
    private Long workPlaceRecOrgId;
    private String workPlaceStateCode;
    private String workPlaceCityCode;

    //address
    private Long receivedOrgId;
    private String addressStateCode;
    private String addressCityCode;
    private String addressNeighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
