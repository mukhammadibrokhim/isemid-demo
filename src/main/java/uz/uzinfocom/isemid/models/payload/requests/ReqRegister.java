package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqRegister {
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
    private ReqRegisterOrganization organization;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReqRegisterOrganization {
        @NotBlank
        private String name;
        @NotBlank
        private String tin;
        @NotBlank
        private String email;
        @NotBlank
        private String phone;
        private String levelCode;
        @NotBlank
        private String countryCode;
        @NotBlank
        private String stateCode;
        @NotBlank
        private String cityCode;
        @NotBlank
        private String line;
        @NotBlank
        private String serviceAreaCode;
    }
}
