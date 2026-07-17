package uz.uzinfocom.app.modules.card.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                @Index(name = "idx_card_form058_id", columnList = "form058_id")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Card extends AbsEntity {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "form058_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_card_form058")
    )
    private Form058 form058;

    @Column(name = "supervisor_comment", length = 1000)
    private String supervisorComment;

    @Column(name = "attached_user_comment", length = 1000)
    private String attachedUserComment;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    private List<Act> acts = new ArrayList<>();

    protected Card(CardType cardType) {
        this.cardType = cardType;
    }
}
