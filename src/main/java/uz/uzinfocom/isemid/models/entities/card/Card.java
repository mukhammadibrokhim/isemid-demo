package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.CardType;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205.class, name = "CARD205"),
        @JsonSubTypes.Type(value = Card205.class, name = "CARD_TUBE")
})
public abstract class Card extends AbsEntity {
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @ManyToMany
    @JoinTable(
            name = "card_users",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();
}
