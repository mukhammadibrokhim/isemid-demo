package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип и категория объекта исследования.")
public record ResearchItemTypeInfoRequest(
        Integer researchTypeId,
        String researchTypeNameUz,
        String researchTypeNameRu,
        Integer categoryId,
        String categoryNameUz,
        String categoryNameRu,
        Integer itemTypeId,
        String itemTypeNameUz,
        String itemTypeNameRu
) {
}
