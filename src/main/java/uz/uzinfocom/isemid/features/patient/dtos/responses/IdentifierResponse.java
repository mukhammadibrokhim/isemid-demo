package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponseDto;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentifierResponse {
    private Long id;
    private String value;
    private CatalogResponseDto typeCode;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
