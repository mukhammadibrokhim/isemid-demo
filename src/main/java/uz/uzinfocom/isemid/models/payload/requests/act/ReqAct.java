package uz.uzinfocom.isemid.models.payload.requests.act;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.models.payload.requests.act.act153.ReqAct153;
import uz.uzinfocom.isemid.models.payload.requests.act.act154.ReqAct154;
import uz.uzinfocom.isemid.models.payload.requests.act.act155.ReqAct155;
import uz.uzinfocom.isemid.models.payload.requests.act.act156.ReqAct156;
import uz.uzinfocom.isemid.models.payload.requests.act.act223.ReqAct223;
import uz.uzinfocom.isemid.models.payload.requests.act.act224.ReqAct224;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "actType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReqAct153.class, name = "ACT153"),
        @JsonSubTypes.Type(value = ReqAct154.class, name = "ACT154"),
        @JsonSubTypes.Type(value = ReqAct155.class, name = "ACT155"),
        @JsonSubTypes.Type(value = ReqAct156.class, name = "ACT156"),
        @JsonSubTypes.Type(value = ReqAct223.class, name = "ACT223"),
        @JsonSubTypes.Type(value = ReqAct224.class, name = "ACT224")
})
public abstract class ReqAct {
    @NotNull(message = "act type is required.")
    private ActType actType;
}

