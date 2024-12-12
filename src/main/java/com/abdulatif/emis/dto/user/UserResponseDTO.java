package com.abdulatif.emis.dto.user;


import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.region.Region;
import com.abdulatif.emis.model.user.Permission;
import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.model.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;

    private String username;

    private Status status;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private String mobile;

    private String email;

    private String address;

    private String passportNumber;

    private String pnfl;

    private String inn;

    private String uuid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    protected Date dateOfBirth;

    private Boolean isAnonymous = false;

    private Boolean gender;

    private String nationality;

    private District district;

    private Region region;

    private String passportSerial;

    private Set<Role> roles = new HashSet<>();

    private Set<Permission> permissions = new HashSet<>();

    private List<Organization> parentOrganizations = new ArrayList<>();

    public UserResponseDTO(User user) {
        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            allPermissions.addAll(role.getPermissions());
        }
        this.id = user.getId();
        this.username = user.getUsername();
        this.status = user.getStatus();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.middleName = user.getMiddleName();
        this.phone = user.getPhone();
        this.mobile = user.getMobile();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.passportNumber = user.getPassportNumber();
        this.pnfl = user.getPnfl();
        this.inn = user.getInn();
        this.uuid = user.getUuid();
        this.dateOfBirth = user.getDateOfBirth();
        this.gender = user.getGender();
        this.nationality = user.getNationality();
        this.district = user.getDistrict();
        this.region = user.getRegion();
        this.passportSerial = user.getPassportSerial();
        this.roles = user.getRoles();
        this.permissions = allPermissions;
        this.parentOrganizations = user.getParentOrganizations().stream().toList();
    }
}
