package uz.uzinfocom.app.modules.form058.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form058ReportInfo {

    @Column(name = "journal_form_code", nullable = false, length = 64)
    private String journalFormCode;

    @Column(name = "form_comment", length = 2000)
    private String comment;

    @Column(name = "notifier_full_name", nullable = false)
    private String notifierFullName;

    @Column(name = "card_by_full_name")
    private String cardByFullName;
}