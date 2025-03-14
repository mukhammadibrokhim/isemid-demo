package uz.uzinfocom.isemid.models.entities.act;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;


@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Act153.class, name = "ACT153"),
        @JsonSubTypes.Type(value = Act154.class, name = "ACT154"),
        @JsonSubTypes.Type(value = Act155.class, name = "ACT155"),
        @JsonSubTypes.Type(value = Act156.class, name = "ACT156"),
        @JsonSubTypes.Type(value = Act223.class, name = "ACT223"),
        @JsonSubTypes.Type(value = Act224.class, name = "ACT224")
})
public abstract class Act extends AbsEntity {

    @Enumerated(EnumType.STRING)
    private ActType actType;

    @Enumerated(EnumType.STRING)
    private ActStatus actStatus = ActStatus.NEW;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    @JsonIgnore
    private Card card;
}
