package uz.uzinfocom.isemid.features.security.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationRequest {
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
