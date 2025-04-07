package uz.uzinfocom.isemid.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDto {
    private String token_type;
    private Long expires_in;
    private String access_token;
    private String refresh_token;
}
