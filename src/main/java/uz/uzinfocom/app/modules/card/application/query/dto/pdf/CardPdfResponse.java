package uz.uzinfocom.app.modules.card.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfResponse;

@Schema(description = """
        Сведения для печатной формы карты. Объединяет собственные поля карты (card, те же сырые
        коды справочников, что и в /{id}) со сведениями извещения, к которому карта привязана
        (form058 или form0581, коды уже приведены к человекочитаемым наименованиям) - печатный
        бланк карты всегда содержит шапку с данными пациента, адресом и местом работы/учёбы,
        которых в самой карте нет. Заполнено ровно одно из полей form058/form0581, второе - null.
        """)
public record CardPdfResponse(
        @Schema(description = "Детальные сведения по карте (тот же формат, что и в GET /{id}).")
        CardDetailResponse card,

        @Schema(description = "Сведения формы №058, к которой привязана карта, в печатном виде. "
                + "null, если карта привязана к форме №058-1.", nullable = true)
        Form058PdfResponse form058,

        @Schema(description = "Сведения формы №058-1, к которой привязана карта, в печатном виде. "
                + "null, если карта привязана к форме №058.", nullable = true)
        Form0581PdfResponse form0581
) {
}
