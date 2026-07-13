package uz.uzinfocom.isemid.features.card.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "card",
        indexes = {
                @Index(name = "idx_card_type", columnList = "cardType"),
                @Index(name = "idx_card_status", columnList = "status"),
                @Index(name = "idx_card_assigned_by", columnList = "assignedById"),
                @Index(name = "idx_card_form058", columnList = "form058_id"),
                @Index(name = "idx_card_form0581", columnList = "form058_1_id")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205.class, name = "CARD205"),
        @JsonSubTypes.Type(value = CardTube.class, name = "CARD_TUBE")
})
public abstract class Card extends AbsEntity {

    @Enumerated(EnumType.STRING)
    private CardType cardType;
    private Long assignedById;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "card_users",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form058_id")
    @JsonBackReference("form058-cards")
    private Form058 form058;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form058_1_id")
    @JsonBackReference("form0581-cards")
    private Form0581 form0581;

    @Column(length = 1000)
    private String supervisorComment;

    @Column(length = 1000)
    private String attachedUserComment;
    private LocalDate completedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Act> acts = new ArrayList<>();
}
