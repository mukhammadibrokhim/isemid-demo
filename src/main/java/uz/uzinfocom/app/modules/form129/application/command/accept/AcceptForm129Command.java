package uz.uzinfocom.app.modules.form129.application.command.accept;

public record AcceptForm129Command(
        Long formId,
        String receiverFullName
) {
}
