package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип пробы.")
public record SampleTypeInfoRequest(
        Integer sampleTypeId,
        String sampleTypeUz,
        String sampleTypeRu
) {
}
