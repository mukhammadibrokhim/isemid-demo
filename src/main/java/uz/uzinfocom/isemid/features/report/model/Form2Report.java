package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Entity;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form2Report extends AbsEntity {
    private String name;
}
