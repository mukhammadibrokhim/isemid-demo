package uz.uzinfocom.app.modules.form0581.application.command.update;

import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.util.UUID;

public record UpdateForm0581Result(
        Long id,
        UUID uuid,
        Form0581Status status
) {
}
