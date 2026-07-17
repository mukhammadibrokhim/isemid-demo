package uz.uzinfocom.isemid.features.form0581.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581NotApproveRequest {
    @Size(max = 1000)
    private String comment;
}
