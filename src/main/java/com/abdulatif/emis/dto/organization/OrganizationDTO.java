package com.abdulatif.emis.dto.organization;

import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.model.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {

    private Long id;

    private String name;

    private String shortName;

    private String address;

    private String hierarchy;

    private Organization parent;

    private Long parentId;

    private Status status;

    private String uuid;

    private List<OrganizationDTO> children;


    public OrganizationDTO(Organization organization) {
        setId(organization.getId());
        setName(organization.getName());
        setShortName(organization.getShortName());
        setAddress(organization.getAddress());
        setHierarchy(organization.getHierarchy());
        setStatus(organization.getStatus());
        setUuid(organization.getUuid());
    }



}
