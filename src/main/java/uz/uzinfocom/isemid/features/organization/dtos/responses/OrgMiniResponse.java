package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgMiniResponse {
    private Long id;
    private UUID uuid;
    private String name;
    private CatalogMinResponse level;
    private Long parentId;
    @Builder.Default
    private List<OrgChildResponse> children = new ArrayList<>();
}
