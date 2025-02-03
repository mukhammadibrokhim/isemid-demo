package uz.uzinfocom.isemid.models.payload.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqOrganization {
    private UUID uuid;
    private String name;
    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private String levelCode;
    private String type;
    private String countryCode;
    private String stateCode;
    private String cityCode;
    private String district;
    private String line;
    private String serviceAreaCode;
    private String hierarchy;
    private UUID parentUuid;
}
