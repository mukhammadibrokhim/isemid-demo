package uz.uzinfocom.isemid.features.form129.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.form129.dtos.requests.LabTestResultRequest;
import uz.uzinfocom.isemid.features.form129.dtos.responses.LabTestResultResponse;
import uz.uzinfocom.isemid.features.form129.models.LabTestResult;

@Mapper(componentModel = "spring")
public interface LabTestResultMapper {
    LabTestResult toEntity(LabTestResultRequest request);

    LabTestResultResponse toResponse(LabTestResult entity);
}
