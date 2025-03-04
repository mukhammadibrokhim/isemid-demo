package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.card161.Card161;
import uz.uzinfocom.isemid.models.entities.card.card174.Card174;
import uz.uzinfocom.isemid.models.entities.card.card175.Card175;
import uz.uzinfocom.isemid.models.entities.card.card205.Card205;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205.class, name = "CARD205"),
        @JsonSubTypes.Type(value = Card205.class, name = "CARD_TUBE")
})
public abstract class Card extends AbsEntity {

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
//    @JsonIgnoreProperties({"gender", "country", "city", "state", "organizations", "roles", "addresses"})
    @JsonIgnore
    private User assignedBy;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "card_users",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties({"gender", "country", "city", "state", "organizations", "roles", "addresses",})
    private Set<User> users = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.NEW;
//
//    @Enumerated(EnumType.STRING)
//    private CardProgressStatus progressStatus = CardProgressStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "form058_id")
    @JsonIgnoreProperties("cards")
    private Form058 form058;

    @Column(length = 1000)
    private String comment;
}
