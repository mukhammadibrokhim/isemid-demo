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
    @NotBlank(message = "Value is required and not blank.")
    private String value;
    @NotNull(message = "TypeCode is required and cannot be null.")
    private IdentifierType typeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
