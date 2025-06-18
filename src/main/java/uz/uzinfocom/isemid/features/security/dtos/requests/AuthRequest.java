package uz.uzinfocom.isemid.features.security.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank(message = "NNUZB не должно быть пустым")
    private String nnuzb;

    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
