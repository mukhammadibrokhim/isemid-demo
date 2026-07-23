package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Условия (особые условия отбора либо условия хранения/доставки).")
public record ConditionInfoRequest(
        Integer conditionId,
        String descriptionUz,
        String descriptionRu
) {
}
