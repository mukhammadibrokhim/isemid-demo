package uz.uzinfocom.isemid.features.security.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank
    private String nnuzb;

    @NotBlank
    private String password;
}
