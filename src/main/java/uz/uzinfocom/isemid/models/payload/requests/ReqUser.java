package uz.uzinfocom.isemid.models.payload.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.Gender;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqUser {
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
    private String accessLevelCode;
    private String positionCode;
}
