package uz.uzinfocom.isemid.features.act.dtos.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.enums.ActType;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "actType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Act156Request.class, name = "ACT153"),
        @JsonSubTypes.Type(value = Act154Request.class, name = "ACT154"),
        @JsonSubTypes.Type(value = Act155Request.class, name = "ACT155"),
        @JsonSubTypes.Type(value = Act156Request.class, name = "ACT156"),
        @JsonSubTypes.Type(value = Act223Request.class, name = "ACT223"),
        @JsonSubTypes.Type(value = Act224Request.class, name = "ACT224")
})
public abstract class ActRequest {
    @NotNull(message = "act type is required.")
    private ActType actType;
}
