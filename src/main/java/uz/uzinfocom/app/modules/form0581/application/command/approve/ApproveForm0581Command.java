package uz.uzinfocom.app.modules.form0581.application.command.approve;

public record ApproveForm0581Command(
        Long formId,
        String finalIcd10Code,
        String finalIcd10Name
) {
}
