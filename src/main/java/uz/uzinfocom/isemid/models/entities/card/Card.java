package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.card.card161.Card161;
import uz.uzinfocom.isemid.models.entities.card.card174.Card174;
import uz.uzinfocom.isemid.models.entities.card.card175.Card175;
import uz.uzinfocom.isemid.models.entities.card.card205.Card205;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

import java.time.LocalDate;
import java.util.*;

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

    private Long assignedById;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "card_users",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties({"gender", "country", "city", "state", "organizations", "roles", "addresses",})
    private Set<User> users = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.NEW;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form058_id")
    private Form058 form058;

    @Column(length = 1000)
    private String supervisorComment;

    @Column(length = 1000)
    private String attachedUserComment;
    private LocalDate completedDate;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Act> acts = new ArrayList<>();

    @Transient
    @JsonProperty("actIds")
    public List<Long> getActIds() {
        return acts != null ? acts.stream().map(Act::getId).toList() : Collections.emptyList();
    }
}
