package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqMkb10 {

    @NotBlank(message = "Code is required and not blank.")
    private String code;
    @NotBlank(message = "Name uz is required and not blank.")
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
    private String comment;
    private Integer usageLimit;
}
