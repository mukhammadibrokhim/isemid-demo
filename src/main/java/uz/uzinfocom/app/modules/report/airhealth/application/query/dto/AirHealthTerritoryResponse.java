package uz.uzinfocom.app.modules.report.airhealth.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;

import java.math.BigDecimal;

@Schema(description = "Bir hudud (viloyat yoki tuman) qatori: havo ifloslantiruvchisi darajasi va shu hududdagi "
        + "tasdiqlangan nafas kasalliklari holatlari. Havo qiymatlari µg/m³.")
public record AirHealthTerritoryResponse(
        @Schema(description = "ref_region.code yoki ref_district.code.") String code,
        @Schema(description = "Hudud nomi (so'rov tilida).") String name,
        @Schema(description = "Shu hududda davr ichida o'lchov bergan stansiyalar soni.") long stationCount,
        @Schema(description = "Davr ichidagi o'lchovlar soni (ifloslantiruvchi bo'yicha).") long readingCount,
        @Schema(description = "O'rtacha daraja; o'lchov bo'lmasa null.") Double average,
        @Schema(description = "Eng yuqori o'lchov; o'lchov bo'lmasa null.") Double maximum,
        @Schema(description = "Kunlik o'rtacha me'yordan oshgan kunlar soni.") long exceedanceDays,
        @Schema(description = "Kamida bitta o'lchovi bor kunlar soni.") long daysWithData,
        @Schema(description = "O'rtacha darajaning me'yorga nisbatan holati.") AirStatus status,
        @Schema(description = "Davr ichidagi tasdiqlangan (APPROVED) holatlar, final ICD-10 kod bo'yicha.") long confirmedCases,
        @Schema(description = "Doimiy aholi (davr oxiri yili); topilmasa 0.") long population,
        @Schema(description = "Holatlar / aholi x koef; aholi yo'q bo'lsa null.") BigDecimal casesPerPopulation
) {
}
