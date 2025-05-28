package uz.uzinfocom.isemid.features.user.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionRequest {
    @NotBlank(message = "Поле subject не должно быть пустым")
    private String subject;
    @NotEmpty(message = "Список действий не должен быть пустым")
    @Valid
    private List<@NotBlank(message = "Действие не может быть пустым") String> actions;
    private Boolean active;
    @NotBlank(message = "Описание (на узбекском языке) не должно быть пустым")
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionUzCyril;
    private String descriptionKaa;
}
