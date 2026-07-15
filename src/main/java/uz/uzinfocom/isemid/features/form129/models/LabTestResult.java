package uz.uzinfocom.isemid.features.form129.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Embeddable
public class LabTestResult {

    @Enumerated(EnumType.STRING)
    private LabTestResultCode resultCode;

    @Column(length = 1000)
    private String resultDetail;
}
