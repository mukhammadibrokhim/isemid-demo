package uz.uzinfocom.isemid.features.classifier.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManualReportRequest {
    private String code;
    @NotBlank(message = "Short name не может быть пустым")
    private String shortName;

    private String nameUz;
    private String nameUzCyril;
    private String nameRu;
    private String nameKaa;
    private Boolean includeInTotal;

    @NotNull(message = "Список кодов МКБ-10 не может быть null")
    @Size(min = 1, message = "Список кодов МКБ-10 не может быть пустым")
    private Set<String> mkb10Codes;
}
