package uz.uzinfocom.isemid.models.payload.requests.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqForm058ChangeDiagnosis {
    @NotBlank
    private String finalMkb10Code;
    @NotBlank
    private String finalMkb10name;
    @NotNull
    private LocalDateTime diseaseDate;
}
