package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Метод консервации пробы.")
public record ConservationTypeInfoResponse(
        @Schema(description = "Идентификатор метода консервации (по справочнику).")
        Integer conservationMethodId,

        @Schema(description = "Наименование метода консервации (узб.).")
        String conservationMethodsUz,

        @Schema(description = "Наименование метода консервации (рус.).")
        String conservationMethodsRu
) {
}
