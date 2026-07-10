package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581AnimalInfo {

    @Column(name = "animal_category_code", length = 64)
    private String animalCategoryCode;

    @Column(name = "animal_color", length = 255)
    private String animalColor;

    @Column(name = "animal_type", length = 255)
    private String animalType;

    @Column(name = "animal_breed", length = 255)
    private String animalBreed;
}
