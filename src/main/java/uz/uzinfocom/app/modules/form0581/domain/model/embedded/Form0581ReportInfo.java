package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581ReportInfo {

    @Column(name = "antirabic_assistance_info", length = 2000)
    private String antirabicAssistanceInfo;

    @Column(name = "notifier_full_name", nullable = false)
    private String notifierFullName;

    @Column(name = "receiver_full_name")
    private String receiverFullName;

    @Column(name = "message_sent_at")
    private LocalDateTime messageSentAt;
}
