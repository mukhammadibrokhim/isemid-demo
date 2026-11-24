package uz.uzinfocom.app.modules.report.airhealth.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.report.shared.PearsonCorrelation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Bir hududning havo darajasi va nafas kasalliklari holatlari vaqt qatori hamda ikkalasi orasidagi "
        + "Pirson korrelyatsiyasi.")
public record AirHealthSeriesResponse(
        String territoryCode,
        String territoryName,
        @Schema(description = "Ifloslantiruvchi kodi.", example = "PM2.5") String parameter,
        @Schema(description = "Daraja birligi.", example = "µg/m³") String unit,
        @Schema(description = "Ifloslantiruvchining me'yori (µg/m³); yo'q bo'lsa null.") BigDecimal norm,
        @Schema(description = "ICD-10 prefiksi.", example = "J") String diagnosisPrefix,
        AirHealthBucket bucket,
        @Schema(description = "Holatlar havo darajasidan necha kun keyin hisoblanadi (kechikish).") int lagDays,
        LocalDate from,
        LocalDate to,
        List<Point> points,
        Correlation correlation
) {

    @Schema(description = "Bir interval.")
    public record Point(
            @Schema(description = "Interval boshi.") LocalDate bucketStart,
            @Schema(description = "O'rtacha daraja; shu intervalda o'lchov bo'lmasa null.") Double airAverage,
            @Schema(description = "Eng yuqori o'lchov; o'lchov bo'lmasa null.") Double airMaximum,
            @Schema(description = "Intervaldagi o'lchovlar soni.") long readings,
            @Schema(description = "O'rtacha daraja me'yordan yuqorimi.") boolean aboveNorm,
            @Schema(description = "Tasdiqlangan holatlar (kechikish hisobga olingan).") long cases
    ) {
    }

    @Schema(description = "Havo darajasi va holatlar orasidagi bog'liqlik. Faqat ikkalasi ham bor intervallar juftlashadi.")
    public record Correlation(
            @Schema(description = "Pirson r [-1, 1]; aniqlab bo'lmasa (3 juftdan kam yoki qator o'zgarmasa) null.")
            Double coefficient,
            @Schema(description = "Ishlatilgan juftlar soni.") int pairs,
            @Schema(description = "Kuchi: NONE (<0.1), WEAK, MODERATE (>=0.3), STRONG (>=0.5); r yo'q bo'lsa null.")
            PearsonCorrelation.Strength strength,
            @Schema(description = "Juftlar soni kamida 14 — koeffitsiyent ishonchli. Aks holda faqat taxminiy.")
            boolean reliable
    ) {
    }
}
