package uz.uzinfocom.app.modules.form129.application.command.create;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.util.UUID;

public record CreateForm129Result(
        Long id,
        UUID uuid,
        Form129Status status
) {
}
