package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrgChildResponse {
    private Long id;
    private UUID uuid;
    private String name;
    private Long parentId;
    private List<OrgChildResponse> children;
}
