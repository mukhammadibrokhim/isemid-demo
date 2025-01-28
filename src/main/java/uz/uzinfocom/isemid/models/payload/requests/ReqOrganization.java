package uz.uzinfocom.isemid.models.payload.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqOrganization {
    private UUID uuid;
    private String name;
    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private String level;
    private String type;
    private String country;
    private String state;
    private String city;
    private String district;
    private String line;
    private String serviceArea;
    private String hierarchy;
    private UUID parentUuid;
}
