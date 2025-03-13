package uz.uzinfocom.isemid.models.payload.requests.act;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.enums.ActType;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "actType", visible = true)
@JsonSubTypes({
       // @JsonSubTypes.Type(value = Act153.class, name = "ACT153"),
        @JsonSubTypes.Type(value = ReqAct154.class, name = "ACT154"),
       // @JsonSubTypes.Type(value = Act155.class, name = "ACT155"),
       // @JsonSubTypes.Type(value = Act156.class, name = "ACT156"),
        //@JsonSubTypes.Type(value = Act223.class, name = "ACT223"),
        //@JsonSubTypes.Type(value = Act224.class, name = "ACT224")
})
public abstract class ReqAct {

    @NotNull(message = "act type is required.")
    private ActType actType;
    //    @NotNull(message = "User id is required.")
    private long userId;

}

