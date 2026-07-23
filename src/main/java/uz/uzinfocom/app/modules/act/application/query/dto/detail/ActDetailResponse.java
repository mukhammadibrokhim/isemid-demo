package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

/**
 * Common contract for every per-type act detail response, matching
 * {@code uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse}'s
 * shape exactly — one concrete record per {@link ActType}, discriminated by
 * the "type" field. Used for both {@code GET /v1/acts/{id}} (workflow +
 * structured data) and {@code GET /v1/acts/{id}/pdf} (same content — Act's
 * embeddables already carry human-readable uz/ru names alongside their
 * codes, so there is no separate raw-code-vs-resolved-name split to make
 * here the way there is for Form058).
 */
@Schema(
        description = "Детальные сведения по акту. Конкретная структура зависит от поля \"type\" — оно определяет, "
                + "какой из 6 типов актов (ACT153, ACT154, ACT155, ACT156, ACT223, ACT224) возвращается.",
        oneOf = {Act153DetailResponse.class, Act154DetailResponse.class, Act155DetailResponse.class,
                Act156DetailResponse.class, Act223DetailResponse.class, Act224DetailResponse.class},
        discriminatorProperty = "type"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Act153DetailResponse.class, name = "ACT153"),
        @JsonSubTypes.Type(value = Act154DetailResponse.class, name = "ACT154"),
        @JsonSubTypes.Type(value = Act155DetailResponse.class, name = "ACT155"),
        @JsonSubTypes.Type(value = Act156DetailResponse.class, name = "ACT156"),
        @JsonSubTypes.Type(value = Act223DetailResponse.class, name = "ACT223"),
        @JsonSubTypes.Type(value = Act224DetailResponse.class, name = "ACT224")
})
public sealed interface ActDetailResponse
        permits Act153DetailResponse, Act154DetailResponse, Act155DetailResponse,
                Act156DetailResponse, Act223DetailResponse, Act224DetailResponse {

    Long id();

    ActType type();

    ActStatus status();

    /**
     * Brief info about the card this act is attached to — not the full
     * card detail, just enough for the act view to identify and label it
     * without a separate round trip.
     */
    CardMiniResponse card();

    Long assignedById();

    String resultComment();

    ActInstitutionResponse institution();

    /**
     * Populated for {@code GET /v1/acts/{id}} (who and when created/updated
     * the act); {@code null} for {@code GET /v1/acts/{id}/pdf}, which has no
     * use for it.
     */
    AuditResponse audit();
}
