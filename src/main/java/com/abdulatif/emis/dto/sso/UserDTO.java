package com.abdulatif.emis.dto.sso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private String name;
    private List<OrganizationDTO> organization;
    private String practitioner;
    private List<String> roles;
    private String phoneVerified;
}