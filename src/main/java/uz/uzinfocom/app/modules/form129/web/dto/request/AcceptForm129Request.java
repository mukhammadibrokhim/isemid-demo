package uz.uzinfocom.app.modules.form129.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на приём формы №129 получателем.")
public record AcceptForm129Request(
        @Schema(description = "ФИО лица, принявшего сообщение в организации-получателе.")
        @Size(max = 255, message = "{validation.form129.receiver-full-name.size}")
        String receiverFullName
) {
}
