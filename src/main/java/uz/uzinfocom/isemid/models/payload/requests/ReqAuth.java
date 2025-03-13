package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqAuth {
    @NotBlank
    private String nnuzb;
    @NotBlank
    private String password;
}
