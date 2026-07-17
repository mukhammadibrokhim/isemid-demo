package uz.uzinfocom.app.modules.act.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * The Act workflow deliberately mirrors {@link Card}'s: a supervisor assigns
 * one or more blank acts (via {@code assignActs}) to a set of employees, who
 * accept/reject, fill it in through {@code resultComment}, complete it, and
 * a supervisor approves or rejects it. The legacy module's 6 act subtypes
 * (act153/154/155/156/223/224, each with their own fields) remain out of
 * scope — this stays a single, generic entity with one free-text outcome
 * field until a full Act module is designed separately.
 */
@Getter
@Setter
@Entity
@Table(
        name = "act",
        indexes = {
                @Index(name = "idx_act_card_id", columnList = "card_id"),
                @Index(name = "idx_act_status", columnList = "act_status"),
                @Index(name = "idx_act_assigned_by", columnList = "assigned_by_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class Act extends AbsEntity {

    @Column(name = "act_type", nullable = false, length = 50)
    private String actType;

    @Enumerated(EnumType.STRING)
    @Column(name = "act_status", nullable = false, length = 32)
    private ActStatus actStatus = ActStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "card_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_act_card")
    )
    private Card card;

    @Column(name = "card_id", insertable = false, updatable = false)
    private Long cardId;

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

    @Column(name = "supervisor_comment", length = 1000)
    private String supervisorComment;

    @Column(name = "attached_user_comment", length = 1000)
    private String attachedUserComment;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    /**
     * The single generic outcome/finding field filled in via {@code update}
     * while the legacy per-subtype data model remains out of scope.
     */
    @Column(name = "result_comment", columnDefinition = "text")
    private String resultComment;
}
