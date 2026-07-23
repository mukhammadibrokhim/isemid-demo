package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInfo {

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "position_uz")
    private String positionUz;

    @Column(name = "position_ru")
    private String positionRu;
}
