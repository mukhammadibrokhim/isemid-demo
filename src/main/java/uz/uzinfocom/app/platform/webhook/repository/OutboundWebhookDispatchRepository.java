package uz.uzinfocom.app.platform.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.webhook.domain.OutboundWebhookDispatch;

import java.time.Instant;
import java.util.List;

public interface OutboundWebhookDispatchRepository
        extends JpaRepository<OutboundWebhookDispatch, Long>, JpaSpecificationExecutor<OutboundWebhookDispatch> {

    /**
     * Multi-instance-safe poll: {@code FOR UPDATE SKIP LOCKED} means two app
     * instances polling at the same moment never block on, or double-claim,
     * each other's rows - a poller simply skips whatever another poller
     * currently has locked. Must run inside a real (non-read-only) caller
     * transaction, e.g. {@code OutboundWebhookDispatchService.pollDueDispatchIds} -
     * Postgres rejects {@code SELECT ... FOR UPDATE} inside a read-only one,
     * which is what Spring Data's default repository transaction would be.
     * The final safety net against a duplicate send even if two pollers both
     * select the same row in the narrow window before either commits is
     * {@code OutboundWebhookDispatchService#markSending}, which only
     * transitions a dispatch still in {@code PENDING}.
     */
    @Query(value = """
            SELECT * FROM outbound_webhook_dispatch
            WHERE status = 'PENDING' AND next_attempt_at <= :now
            ORDER BY next_attempt_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboundWebhookDispatch> findDueForDispatch(@Param("now") Instant now, @Param("batchSize") int batchSize);
}
