package uz.uzinfocom.app.platform.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.time.Instant;

/**
 * One row per audited business event (creation, status change, or receiving-organization
 * reassignment) on Form058/Form0581/Act — never a generic whole-entity snapshot, only the
 * handful of columns those three event kinds actually carry. Written asynchronously,
 * after the originating transaction commits — see {@code AuditEventListener}. Not an
 * {@link uz.uzinfocom.app.platform.persistence.entity.AuditableEntity}: this row IS the
 * audit record, there's no separate "creator" to attribute it to beyond {@link #actorUserId}.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_event")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "old_org_id")
    private Long oldOrgId;

    @Column(name = "new_org_id")
    private Long newOrgId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
