package uz.uzinfocom.isemid.features.form0581.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581CancelRequest {
    @NotBlank
    private String cancelReason;
}
