package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "Обновление списка разрешённых IPv4-адресов/CIDR-блоков интеграционного клиента.")
public record IntegrationClientAllowedIpsUpdateRequest(

        @Schema(description = "IPv4-адреса или CIDR-блоки, с которых разрешено обращаться этому клиенту "
                + "(например, \"10.0.5.0/24\"). Пустой список или null — ограничений нет.",
                example = "10.0.5.0/24")
        List<@NotBlank(message = "{integration-client.allowed-ips.invalid}") String> allowedIps
) {
}
