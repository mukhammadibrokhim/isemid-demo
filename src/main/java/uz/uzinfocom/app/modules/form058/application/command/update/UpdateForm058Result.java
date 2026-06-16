package uz.uzinfocom.app.modules.form058.application.command.update;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

public record UpdateForm058Result(
        Long id,
        UUID uuid,
        FormStatus status
) {
}
