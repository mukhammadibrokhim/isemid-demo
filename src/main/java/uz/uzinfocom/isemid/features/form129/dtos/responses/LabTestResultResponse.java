package uz.uzinfocom.isemid.features.form129.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.form129.models.enums.LabTestResultCode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabTestResultResponse {
    private LabTestResultCode resultCode;
    private String resultDetail;
}
