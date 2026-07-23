package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Everything about the act's transmission to the external LIS (Laboratory
 * Information System), grouped into one value object rather than flat
 * scalar fields on {@link uz.uzinfocom.app.modules.act.domain.model.Act} —
 * these only ever change together, as part of the same
 * send-to-LIS/receive-response step. Whether the act has been sent/received
 * is tracked on {@code Act.actStatus} itself, not duplicated here.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class LisInfo {

    @Column(name = "lis_attempt")
    private Integer attempt = 0;

    @Column(name = "lis_sent_date")
    private LocalDateTime sentDate;

    @Column(name = "lis_act_id")
    private Long actId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lis_response", columnDefinition = "jsonb")
    private Map<String, Object> response;

    /**
     * Why the most recent send attempt failed, kept so the attached employee
     * can see it without digging through logs. Cleared on the next attempt,
     * so it only ever describes the latest failure — and is null whenever the
     * act is not in {@code SEND_FAILED}.
     */
    @Column(name = "lis_last_error", columnDefinition = "text")
    private String lastError;

    /**
     * Records that a send is being attempted: bumps the attempt counter,
     * stamps the time, and clears any previous failure so a retry doesn't
     * show a stale reason.
     */
    public void markSendAttempt() {
        this.attempt = (this.attempt == null ? 0 : this.attempt) + 1;
        this.sentDate = LocalDateTime.now();
        this.lastError = null;
    }
}
