package uz.uzinfocom.app.modules.form058.application.command.approve;

public record ApproveForm058Command(
        Long formId,
        String finalIcd10Code,
        String finalIcd10Name
) {
}
