package uz.uzinfocom.isemid.features.user.dtos.requests;

import com.nimbusds.openid.connect.sdk.claims.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    private String nnuzb;
    private String ppn;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate birthDate;
    private boolean active;
    private Gender gender;
    private String phoneNumber;
    private String countryCode;
    private String stateCode;
    private String cityCode;
    private String district;
    private String line;
    private String positionCode;
}
