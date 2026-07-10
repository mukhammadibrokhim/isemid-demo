package uz.uzinfocom.app.platform.reference.application.mkb10.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для обновления узла классификатора МКБ-10. Сам идентификатор не редактируется.")
public record Mkb10UpdateRequest(
        @Schema(description = "Устаревший/альтернативный числовой идентификатор из предыдущей системы классификации.",
                example = "1500")
        Long secondaryId,

        @Schema(description = "Внешний идентификатор родительского узла. Null для узлов верхнего уровня (глав).", example = "12")
        @Positive(message = "{reference.mkb10.parent_id.positive}")
        Long parentId,

        @Schema(description = "Уникальный код МКБ-10.", example = "A15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{reference.mkb10.code.required}")
        @Size(max = 20, message = "{reference.mkb10.code.max_length}")
        String code,

        @Schema(description = "Глубина данного узла в иерархии классификатора, начиная с 0.", example = "3")
        @NotNull(message = "{reference.mkb10.level.required}")
        @PositiveOrZero(message = "{reference.mkb10.level.positive_or_zero}")
        Integer level,

        @Schema(description = "Признак того, что узел является конечным (назначаемым кодом диагноза), а не заголовком категории.",
                example = "true")
        @NotNull(message = "{reference.mkb10.last_level.required}")
        Boolean lastLevel,

        @Schema(description = "Наименование диагноза на узбекском языке (латиница).", example = "Tuberkulyoz")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование диагноза на узбекском языке (кириллица).", example = "Туберкулёз")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование диагноза на русском языке.", example = "Туберкулёз")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование диагноза на каракалпакском языке.", example = "Túberkulez")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @Schema(description = "Произвольное примечание к данному узлу классификатора.")
        @Size(max = 2000, message = "{reference.mkb10.comment.max_length}")
        String comment,

        @Schema(description = "Сколько раз может быть использован/выбран данный код. По умолчанию 1, если не указано.",
                example = "1")
        @Min(value = 0, message = "{reference.mkb10.usage_limit.min}")
        Integer usageLimit
) {
}
