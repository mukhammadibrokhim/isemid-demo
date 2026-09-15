package uz.uzinfocom.app.modules.form129.application.command.reject;

public record RejectForm129Command(
        Long formId,
        String reason
) {
}
