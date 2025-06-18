package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mkb10Response {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String code;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
}
