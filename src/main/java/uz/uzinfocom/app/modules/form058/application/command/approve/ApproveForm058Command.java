package uz.uzinfocom.app.modules.form058.application.command.approve;

public record ApproveForm058Command(
        Long formId,
        String finalMkb10Code,
        String finalMkb10Name
) {
}
