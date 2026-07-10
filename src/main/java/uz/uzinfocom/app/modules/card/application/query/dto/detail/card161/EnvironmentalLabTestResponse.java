package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Результат лабораторного исследования объекта окружающей среды.")
public record EnvironmentalLabTestResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Дата проведения исследования.")
        LocalDate examinationDate,

        @Schema(description = "Обследуемый объект (членистоногие/животные).")
        String objectArthropodsAnimals,

        @Schema(description = "Материал, взятый для исследования.")
        String material,

        @Schema(description = "Количество отобранных проб.")
        String sampleQuantity,

        @Schema(description = "Вид исследования и его результат.")
        String testTypeAndResult
) {
}
