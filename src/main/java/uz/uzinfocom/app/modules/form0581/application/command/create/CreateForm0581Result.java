package uz.uzinfocom.app.modules.form0581.application.command.create;

import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.util.UUID;

public record CreateForm0581Result(
        Long id,
        UUID uuid,
        Form0581Status status
) {
}
