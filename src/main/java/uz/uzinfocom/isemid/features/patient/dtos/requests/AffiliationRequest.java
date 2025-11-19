package uz.uzinfocom.isemid.features.patient.dtos.requests;

import lombok.*;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffiliationRequest {
    private AffiliationType type;
    private LocalDate lastVisitedDate;
    private String organizationName;
    private String stateCode;
    private String cityCode;
    private Long organizationId;
    private UUID organizationUuid;
    private String address;
}
