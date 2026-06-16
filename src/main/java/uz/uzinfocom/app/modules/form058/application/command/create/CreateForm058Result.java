package uz.uzinfocom.app.modules.form058.application.command.create;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

public record CreateForm058Result(
        Long id,
        UUID uuid,
        FormStatus status
) {
}
