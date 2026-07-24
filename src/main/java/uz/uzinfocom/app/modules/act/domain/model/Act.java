package uz.uzinfocom.app.modules.act.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ActDeleteInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Institution;
import uz.uzinfocom.app.modules.act.domain.model.embedded.LisInfo;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * A supervisor assigns one or more blank acts (via {@code assignActs}) to a
 * card and a set of employees, who fill it in and re-save it freely, any
 * number of times; the act is then sent to the external LIS (Laboratory
 * Information System, {@link #lisInfo}) and its response is received back.
 * That is the entire lifecycle — one status ({@link ActStatus}), no
 * accept/reject or supervisor-approval gate, unlike {@link Card}. The 6
 * concrete subtypes (act153/154/155/156/223/224, one {@code @Entity} each
 * under this package's sibling packages) carry the type-specific structured
 * data; JOINED inheritance keeps each subtype's ~15-30 fields out of a
 * single sprawling table.
 */
@Getter
@Setter
@Entity
@Table(
        name = "act",
        indexes = {
                @Index(name = "idx_act_card_id", columnList = "card_id"),
                @Index(name = "idx_act_status", columnList = "act_status"),
                @Index(name = "idx_act_assigned_by", columnList = "assigned_by_id"),
                @Index(name = "idx_act_deleted", columnList = "deleted")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public abstract class Act extends AbsEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "act_type", nullable = false, length = 50)
    private ActType actType;

    @Enumerated(EnumType.STRING)
    @Column(name = "act_status", nullable = false, length = 32)
    private ActStatus actStatus = ActStatus.NEW;

    @Embedded
    private Institution institution;

    @Embedded
    private LisInfo lisInfo = new LisInfo();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "card_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_act_card")
    )
    private Card card;

    @Column(name = "assigned_by_id")
    private Long assignedById;

    /**
     * Indexed on {@code user_id} because {@code GET /acts/mine} filters this
     * join table by user on every request — mirrors {@code Card.users}
     * exactly, including the no-cascade rule (deleting an act must never
     * delete a User).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "act_users",
            joinColumns = @JoinColumn(name = "act_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_act_users_act_user", columnNames = {"act_id", "user_id"}),
            indexes = @Index(name = "idx_act_users_user_id", columnList = "user_id")
    )
    private Set<User> users = new HashSet<>();

    /**
     * Free-text fallback outcome field from before the per-subtype data
     * model existed; the concrete subtypes' structured fields are now the
     * source of truth for what was actually found.
     */
    @Column(name = "result_comment", columnDefinition = "text")
    private String resultComment;

    /**
     * Soft delete state.
     * Columns remain in act table:
     * deleted, deleted_at, deleted_by_id, delete_reason.
     */
    @Embedded
    private ActDeleteInfo deleteInfo = new ActDeleteInfo();

    public void softDelete(Long deletedBy, String reason) {
        ensureDeleteInfo();
        this.deleteInfo.softDelete(deletedBy, reason);
    }

    public void restore() {
        ensureDeleteInfo();
        this.deleteInfo.restore();
    }

    public boolean isDeleted() {
        return this.deleteInfo != null && this.deleteInfo.isDeleted();
    }

    private void ensureDeleteInfo() {
        if (this.deleteInfo == null) {
            this.deleteInfo = new ActDeleteInfo();
        }
    }
}
