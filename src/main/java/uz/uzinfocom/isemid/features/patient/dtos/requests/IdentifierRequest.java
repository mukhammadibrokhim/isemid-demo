package uz.uzinfocom.isemid.features.patient.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.patient.enums.IdentifierType;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentifierRequest {
    @NotBlank(message = "Значение обязательно и не может быть пустым.")
    private String value;
    @NotNull(message = "Типовой код обязателен и не может быть нулевым.")
    private IdentifierType typeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
