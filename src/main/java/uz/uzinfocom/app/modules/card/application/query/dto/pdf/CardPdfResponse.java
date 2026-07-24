package uz.uzinfocom.app.modules.card.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;

@Schema(description = """
        Сведения для печатной формы карты. Объединяет собственные поля карты (card, те же сырые
        коды справочников, что и в /{id}) со сведениями формы №058, к которой карта привязана
        (form058, коды уже приведены к человекочитаемым наименованиям) - печатный бланк карты
        всегда содержит шапку с данными пациента, адресом и местом работы/учёбы, которых в самой
        карте нет.
        """)
public record CardPdfResponse(
        @Schema(description = "Детальные сведения по карте (тот же формат, что и в GET /{id}).")
        CardDetailResponse card,

        @Schema(description = "Сведения формы №058, к которой привязана карта, в печатном виде.")
        Form058PdfResponse form058
) {
}
