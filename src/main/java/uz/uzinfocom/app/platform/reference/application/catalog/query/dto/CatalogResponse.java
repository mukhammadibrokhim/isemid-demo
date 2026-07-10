package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

@Schema(description = "Детальный ответ по элементу каталога.")
public record CatalogResponse(
        @Schema(description = "Внутренний идентификатор элемента каталога.", example = "1")
        Long id,
        @Schema(description = "Тип каталога.", example = "GENDER")
        String type,
        @Schema(description = "Уникальный код элемента внутри выбранного типа каталога.")
        String code,
        @Schema(description = "Необязательный код родительского элемента внутри того же типа каталога.")
        String parentCode,
        @Schema(description = "Наименование элемента каталога на узбекском языке (латиница).")
        String nameUz,
        @Schema(description = "Наименование элемента каталога на узбекском языке (кириллица).")
        String nameUzCyril,
        @Schema(description = "Наименование элемента каталога на русском языке.")
        String nameRu,
        @Schema(description = "Наименование элемента каталога на каракалпакском языке.")
        String nameKaa,
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted,
        @Schema(description = "Сведения об аудите записи.")
        AuditResponse audit
) {
}
