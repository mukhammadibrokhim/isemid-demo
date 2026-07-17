package uz.uzinfocom.isemid.features.form0581.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Information about the biting animal (DE.73, DE.78-80) - modeled as a single cohesive value object.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class AnimalInfo {

    @Column(name = "animal_category_code")
    private String categoryCode;

    @Column(name = "animal_color")
    private String color;

    @Column(name = "animal_type")
    private String type;

    @Column(name = "animal_breed")
    private String breed;
}
