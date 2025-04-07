package uz.uzinfocom.isemid.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private UUID practitioner;
    private String name;
    private List<OrganizationDto> organization;
    private List<String> roles;
    @JsonProperty("phone_verified")
    private Boolean phoneVerified;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrganizationDto {
        private UUID uuid;
        private String name;
        private Boolean active;
        @JsonProperty("practitioner_role")
        private List<PractitionerRoleDto> practitionerRole;
    }

    @Getter
    @Setter
    public static class PractitionerRoleDto {
        private UUID uuid;
        private String code;
    }
}
