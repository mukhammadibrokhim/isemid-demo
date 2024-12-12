package com.abdulatif.emis.dto.user;

import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.region.Region;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private String email;

    private String address;

    private String passportSerial;

    private String passportNumber;

    private String pnfl;

    private String inn;

    private String uuid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    protected Date dateOfBirth;

    private Boolean isAnonymous;

    private Boolean gender;

    private String nationality;

    private District district;

    private Long districtId;

    private Region region;

    private Long regionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date personalDataIssuedDate;
}
