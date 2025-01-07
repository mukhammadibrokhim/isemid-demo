package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.IdentifierType;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReqIdentifier {
    @NotBlank(message = "Значение обязательно и не может быть пустым.")
    private String value;
    @NotNull(message = "Типовой код обязателен и не может быть нулевым.")
    private IdentifierType typeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
