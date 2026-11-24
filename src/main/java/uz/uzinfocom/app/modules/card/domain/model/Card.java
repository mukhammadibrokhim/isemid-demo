package uz.uzinfocom.app.modules.card.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.embedded.CardDeleteInfo;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.platform.audit.domain.AuditFieldReflector;
import uz.uzinfocom.app.platform.audit.domain.AuditableFields;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;
import java.util.*;

/**
 * Base type for the five epidemiological card types (JOINED inheritance — each
 * subtype gets its own table). Rebuilt from the legacy
 * {@code uz.uzinfocom.isemid.features.card.models.Card}, fixing:
 * <ul>
 *   <li>{@code users} had {@code cascade = CascadeType.ALL}, which deleted
 *       User rows when a card was deleted — removed here.</li>
 *   <li>{@code cardType} was freely settable — now constructor-assigned per
 *       subtype and immutable. No {@code @Builder}/{@code @SuperBuilder} on
 *       this hierarchy at all: a builder would let a caller set
 *       {@code cardType} independently of the concrete subclass, which is
 *       exactly the bug being fixed.</li>
 *   <li>{@code form058} was nullable — a card cannot exist without a form,
 *       enforced at both the Java and DB level.</li>
 * </ul>
 * A card belongs to exactly one case — either {@link #form058} or {@link
 * #form0581}, never both, never neither. Both associations are therefore
 * nullable at the Java/JPA level; the DB enforces "exactly one set" via the
 * {@code chk_card_exactly_one_form} check constraint (see
 * {@code zzz-card-act/20260729-1000-add-card-form0581-support.xml}). Card
 * type is restricted for form0581 cases (only {@code CARD174}/{@code CARD175}/{@code CARD205}
 * — the zoonotic/animal-bite types) at the {@code CardCommandService}
 * validation level, not by the schema itself.
 * Polymorphism (the JSON "type" discriminator) lives entirely in the DTO
 * layer ({@code CardRequest}/{@code CardDetailResponse}) — this entity has
 * no Jackson annotations.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card",
        indexes = {
                @Index(name = "idx_card_type", columnList = "card_type"),
                @Index(name = "idx_card_status", columnList = "status"),
                @Index(name = "idx_card_assigned_by", columnList = "assigned_by_id"),
                @Index(name = "idx_card_form058_id", columnList = "form058_id"),
                @Index(name = "idx_card_form058_1_id", columnList = "form058_1_id"),
                @Index(name = "idx_card_deleted", columnList = "deleted")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Card extends AbsEntity implements AuditableFields {

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, updatable = false, length = 20)
    private CardType cardType;

    @Column(name = "assigned_by_id")
    private Long assignedById;

    /**
     * Indexed on {@code user_id} because {@code GET /cards/mine}
     * filters this join table by user on every request — without it, that query
     * degrades to a full scan of {@code card_users} as it grows. The
     * unique constraint doubles as the index for the reverse direction
     * ({@code card_id} lookups) and enforces at the DB level what the
     * Java-side {@code Set} already guarantees in memory: a user can't be
     * attached to the same card twice.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "card_users",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_card_users_card_user", columnNames = {"card_id", "user_id"}),
            indexes = @Index(name = "idx_card_users_user_id", columnList = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CardStatus status = CardStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "form058_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_card_form058")
    )
    private Form058 form058;

    /**
     * Column is {@code form058_1_id} (not {@code form0581_id}) — the
     * form0581 module's table is named {@code form058_1}
     * (form0581/20260710-1000-create-form0581.xml), matching that so the FK
     * naming stays consistent with {@link #form058}'s own {@code
     * form058_id}/{@code fk_card_form058} pair.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "form058_1_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_card_form058_1")
    )
    private Form0581 form0581;

    @Column(name = "supervisor_comment", length = 1000)
    private String supervisorComment;

    @Column(name = "attached_user_comment", length = 1000)
    private String attachedUserComment;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    private List<Act> acts = new ArrayList<>();

    /**
     * Soft delete state.
     * Columns remain in card table:
     * deleted, deleted_at, deleted_by_id, delete_reason.
     */
    @Embedded
    private CardDeleteInfo deleteInfo = new CardDeleteInfo();

    protected Card(CardType cardType) {
        this.cardType = cardType;
    }

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

    public boolean isAttachedToForm0581() {
        return this.form0581 != null;
    }

    private void ensureDeleteInfo() {
        if (this.deleteInfo == null) {
            this.deleteInfo = new CardDeleteInfo();
        }
    }

    /**
     * Delegates to {@link AuditFieldReflector} rather than hand-listing
     * fields: each of the 5 subtypes (Card161/174/175/205/CardTube) has
     * dozens of its own scalar columns, and a hand-written list would drift
     * out of sync as those evolve. The reflector already excludes
     * collections/child lists and entity-typed associations (e.g.
     * {@link #form058}, {@link #acts}, {@code Card161.polyclinic}) by their
     * declared type, and stops at {@link AbsEntity} so version/updatedAt/etc.
     * never pollute the diff.
     */
    @Override
    public Map<String, Object> auditFields() {
        return AuditFieldReflector.reflect(this, AbsEntity.class);
    }
}
