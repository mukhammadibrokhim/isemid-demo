package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;

import java.util.List;

@Schema(description = "Обновление интеграционного клиента. clientId, authType, sourceKey, organizationId "
        + "и учётные данные не изменяются здесь — для их смены отзовите клиента и зарегистрируйте нового.")
public record IntegrationClientUpdateRequest(

        @Schema(description = "Человекочитаемое название интеграции.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration-client.name.required}")
        @Size(max = 255, message = "{integration-client.name.size}")
        String name,

        @Schema(description = "Права доступа, предоставляемые клиенту.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "{integration-client.scopes.required}")
        List<IntegrationScope> scopes,

        @Schema(description = "true — клиент может аутентифицироваться; false — отклоняется на любом "
                + "запросе немедленно, независимо от способа аутентификации (эквивалентно отзыву, но "
                + "обратимо — в отличие от POST .../revoke).", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{integration-client.active.required}")
        Boolean active
) {
}
