package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespOrganizationDto {
    private Long id;
    private UUID uuid;
    private String name;
}
