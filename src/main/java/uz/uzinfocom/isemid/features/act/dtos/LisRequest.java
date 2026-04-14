package uz.uzinfocom.isemid.features.act.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.Conditions;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.NoteConditions;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.PackageType;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.PreservationMethod;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.Priority;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.Purpose;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.SelectionActItem;

@Builder
public record LisRequest(
    Long actId,
    Long lisLabId,

    Integer actTemplateId,
    Priority priority,
    String tin,
    String organizationName,
    String organizationAddress,
    String organizationLegalAddress,
    Purpose purpose,  // @NotNull(message = "purpose must not be null") //front
    LocalDateTime sampleTakenDate, // front
    LocalDate deliveryDateToLaboratory,  // front
    Conditions conditions, // @NotNull(message = "conditions must not be null")  //front
    String additionalInformation,  // front
    String involvedPersonName, // front
    Integer involvedProfessionId, // front
    PackageType packageType, // front
    String document, // front
    String goal, //  front
    NoteConditions noteConditions, //front
    Integer collectorProfessionId, // front
    String manufacturer, // front
    LocalDate manufactureDate, // front
    String docNumber, // front
    PreservationMethod preservationMethod, //front
    Boolean paid, // front (dropdown)
    List<SelectionActItem> selectionActItems,// @NotEmpty(message = "selection act items must not be null")
    String locationOwnership // front

) {
}
