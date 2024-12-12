package com.abdulatif.emis.dto.sso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationDTO {
    private String uuid;
    private String name;
    private Boolean active;
    @JsonProperty("practitioner_role")
    private List<PractitionerRoleDTO> practitionerRole;
}
