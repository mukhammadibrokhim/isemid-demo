package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PermissionResponse {
    private String subject;
    private boolean active;
    private List<String> actions;
}
