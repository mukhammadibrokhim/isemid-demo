package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Учётные данные вновь зарегистрированного интеграционного клиента. "
        + "clientSecret возвращается единственный раз — сохраните его сейчас, повторно получить будет нельзя.")
public record IntegrationClientCreateResponse(
        Long id,
        String clientId,

        @Schema(description = "Секрет клиента в открытом виде. Показывается только один раз, при создании.")
        String clientSecret,

        String name,

        @Schema(description = "Сегмент URL этого клиента, например /integration/dmed/form-058.")
        String sourceKey,

        Long organizationId,
        List<String> scopes
) {
}
