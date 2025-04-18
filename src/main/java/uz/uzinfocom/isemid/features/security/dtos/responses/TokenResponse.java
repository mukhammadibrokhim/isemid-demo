package uz.uzinfocom.isemid.features.security.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String token_type;
    private Long expires_in;
    private String access_token;
    private String refresh_token;
}
