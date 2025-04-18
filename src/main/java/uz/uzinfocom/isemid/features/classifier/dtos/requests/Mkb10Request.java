package uz.uzinfocom.isemid.features.classifier.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mkb10Request {
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
