package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserMiniResponse {
    private Long id;
    private UUID uuid;
    private String nnuzb;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fullName;
    private LocalDate birthDate;
}
