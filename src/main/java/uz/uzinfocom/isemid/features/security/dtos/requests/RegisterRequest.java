package uz.uzinfocom.isemid.features.security.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Nnuzb(pinfl) is required.")
    private String nnuzb;
    @NotBlank
    private String ppn;
    @NotBlank
    private String password;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String middleName;
    private LocalDate birthDate;
    private boolean active;
    private String phoneNumber;
    @NotBlank
    private String countryCode;
    @NotBlank
    private String stateCode;
    @NotBlank
    private String cityCode;
    private String district;
    private String line;
    @NotBlank
    private String genderCode;
    @NotBlank
    private String positionCode;

    @Valid
    private OrganizationRequest organization;
}
