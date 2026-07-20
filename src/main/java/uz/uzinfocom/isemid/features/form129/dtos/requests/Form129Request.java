package uz.uzinfocom.isemid.features.form129.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.patient.dtos.requests.PatientRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form129Request {

    private String medRecordId;

    @Valid
    @NotNull(message = "Bemor obyekti majburiy.")
    private PatientRequest patient;

    @NotNull(message = "Yuboruvchi tashkilot majburiy va bo'sh bo'lishi mumkin emas.")
    private UUID senderOrganizationId;

    @NotNull(message = "Qabul qiluvchi tashkilot majburiy va bo'sh bo'lishi mumkin emas.")
    private UUID receiverOrganizationId;

    @Valid
    private LabTestResultRequest rw;

    @Valid
    private LabTestResultRequest rprVdrl;

    @Valid
    private LabTestResultRequest rpga;

    @Valid
    private LabTestResultRequest elisa;

    @Valid
    private LabTestResultRequest tpha;

    @Valid
    private LabTestResultRequest westernBlot;

    @Valid
    private LabTestResultRequest hbsAg;

    @Valid
    private LabTestResultRequest hbeAg;

    @Valid
    private LabTestResultRequest antiHbcIgG;

    @Valid
    private LabTestResultRequest antiHbcIgM;

    @Valid
    private LabTestResultRequest antiHbe;

    @Valid
    private LabTestResultRequest antiHbs;

    @Valid
    private LabTestResultRequest pcr;

    @Valid
    private LabTestResultRequest wrightHeddelson;

    @NotBlank(message = "Xabar beruvchining F.I.Sh. bo'sh bo'lishi mumkin emas.")
    private String notifierFullName;

    private String receiverFullName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Sana majburiy.")
    private LocalDateTime initialReportDateTime;
}
