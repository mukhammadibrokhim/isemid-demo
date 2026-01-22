package uz.uzinfocom.isemid.features.integrations.info.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.integrations.info.dtos.responses.InfoIcd10Response;
import uz.uzinfocom.isemid.features.integrations.info.dtos.responses.InfoResponse;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface InfoMapper {
    InfoResponse toResponse(Form058 form058);

    default InfoIcd10Response toIcd10Response(List<Form058> forms) {
        List<String> mkb10s = forms.stream()
                .filter(f -> f.getStatus() == FormStatus.APPROVED)
                .map(Form058::getFinalMkb10Code)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return InfoIcd10Response.builder()
                .mkb10s(mkb10s)
                .build();
    }
}
