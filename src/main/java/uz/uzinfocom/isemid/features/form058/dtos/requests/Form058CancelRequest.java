package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058CancelRequest {
    @NotBlank
    private String cancelReason;
}
