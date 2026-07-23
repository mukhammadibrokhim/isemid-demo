package uz.uzinfocom.app.modules.act.web.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

/**
 * Common contract for every per-type act save request, matching
 * {@code uz.uzinfocom.app.modules.card.web.dto.request.CardRequest}'s shape
 * exactly. Polymorphism lives only here, in the DTO layer — the JPA
 * entities have no Jackson annotations at all.
 */
@Schema(
        description = "Данные акта для сохранения. Конкретная структура зависит от поля \"type\" — оно определяет, "
                + "какой из 6 типов актов (ACT153, ACT154, ACT155, ACT156, ACT223, ACT224) заполняется.",
        oneOf = {Act153Request.class, Act154Request.class, Act155Request.class,
                Act156Request.class, Act223Request.class, Act224Request.class},
        discriminatorProperty = "type"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Act153Request.class, name = "ACT153"),
        @JsonSubTypes.Type(value = Act154Request.class, name = "ACT154"),
        @JsonSubTypes.Type(value = Act155Request.class, name = "ACT155"),
        @JsonSubTypes.Type(value = Act156Request.class, name = "ACT156"),
        @JsonSubTypes.Type(value = Act223Request.class, name = "ACT223"),
        @JsonSubTypes.Type(value = Act224Request.class, name = "ACT224")
})
public sealed interface ActRequest permits Act153Request, Act154Request, Act155Request, Act156Request, Act223Request, Act224Request {

    ActType type();
}
