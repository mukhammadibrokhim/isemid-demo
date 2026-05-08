package uz.uzinfocom.isemid.features.act.dtos.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.act153.Act153Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act154.Act154Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act155.Act155Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act156.Act156Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act223.Act223Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act224.Act224Response;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Act153Response.class, name = "ACT153"),
        @JsonSubTypes.Type(value = Act154Response.class, name = "ACT154"),
        @JsonSubTypes.Type(value = Act155Response.class, name = "ACT155"),
        @JsonSubTypes.Type(value = Act156Response.class, name = "ACT156"),
        @JsonSubTypes.Type(value = Act223Response.class, name = "ACT223"),
        @JsonSubTypes.Type(value = Act224Response.class, name = "ACT224")
})
public abstract class ActResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private OrgMiniResponse createdOrg;
    private ActStatus status;
    private ActType type;
    private Long cardId;
    ActSubjectInfoResponse actSubjectInfo;
    private List<UserMiniResponse> users;
    private LocalDateTime lisSentDate;
    private Integer lisAttempt;
}
