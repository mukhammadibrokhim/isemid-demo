package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058NotApproveRequest {
    @Size(max = 1000)
    String comment;
}
