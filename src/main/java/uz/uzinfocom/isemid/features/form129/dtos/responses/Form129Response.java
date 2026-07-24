package uz.uzinfocom.isemid.features.form129.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form129Response {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String medRecordId;
    private PatientResponse patient;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;

    private LabTestResultResponse rw;
    private LabTestResultResponse rprVdrl;
    private LabTestResultResponse rpga;
    private LabTestResultResponse elisa;
    private LabTestResultResponse tpha;
    private LabTestResultResponse westernBlot;
    private LabTestResultResponse hbsAg;
    private LabTestResultResponse hbeAg;
    private LabTestResultResponse antiHbcIgG;
    private LabTestResultResponse antiHbcIgM;
    private LabTestResultResponse antiHbe;
    private LabTestResultResponse antiHbs;
    private LabTestResultResponse pcr;
    private LabTestResultResponse wrightHeddelson;

    private String notifierFullName;
    private String receiverFullName;
    private LocalDateTime initialReportDateTime;
}
