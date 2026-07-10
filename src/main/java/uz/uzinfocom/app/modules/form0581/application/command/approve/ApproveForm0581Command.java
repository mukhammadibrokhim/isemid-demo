package uz.uzinfocom.app.modules.form0581.application.command.approve;

public record ApproveForm0581Command(
        Long formId,
        String finalMkb10Code,
        String finalMkb10Name
) {
}
