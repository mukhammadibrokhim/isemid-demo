package uz.uzinfocom.isemid.features.act.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;
import uz.uzinfocom.isemid.features.card.models.Card;


@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
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
    @Column(name = "act_type", nullable = false, columnDefinition = "TEXT")
    private ActType actType;

    @Enumerated(EnumType.STRING)
    private ActStatus actStatus = ActStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @JsonIgnore
    private Card card;

    @Column(name = "card_id", insertable = false, updatable = false)
    private Long cardId;
}
