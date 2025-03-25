package uz.uzinfocom.isemid.models.payload.responses.act;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.models.entities.act.ActStatus;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.models.payload.responses.act.act153.RespAct153;
import uz.uzinfocom.isemid.models.payload.responses.act.act154.RespAct154;
import uz.uzinfocom.isemid.models.payload.responses.act.act155.RespAct155;
import uz.uzinfocom.isemid.models.payload.responses.act.act156.RespAct156;
import uz.uzinfocom.isemid.models.payload.responses.act.act223.RespAct223;
import uz.uzinfocom.isemid.models.payload.responses.act.act224.RespAct224;


@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "actType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RespAct153.class, name = "ACT153"),
        @JsonSubTypes.Type(value = RespAct154.class, name = "ACT154"),
        @JsonSubTypes.Type(value = RespAct155.class, name = "ACT155"),
        @JsonSubTypes.Type(value = RespAct156.class, name = "ACT156"),
        @JsonSubTypes.Type(value = RespAct223.class, name = "ACT223"),
        @JsonSubTypes.Type(value = RespAct224.class, name = "ACT224")
})
public class RespAct {
    private ActType actType;
    private ActStatus actStatus = ActStatus.NEW;
    private Long cardId;
}