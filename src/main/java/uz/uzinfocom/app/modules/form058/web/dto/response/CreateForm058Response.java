package uz.uzinfocom.app.modules.form058.web.dto.response;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

public record CreateForm058Response(
        Long id,
        UUID uuid,
        FormStatus status
) {
}
