package uz.uzinfocom.app.modules.form058.web.response;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

public record UpdateForm058Response(
        Long id,
        UUID uuid,
        FormStatus status
) {
}
