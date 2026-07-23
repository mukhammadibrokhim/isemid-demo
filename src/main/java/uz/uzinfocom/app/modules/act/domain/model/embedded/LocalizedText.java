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
public class LocalizedText {

    @Column(name = "uz", columnDefinition = "TEXT")
    private String uz;

    @Column(name = "ru", columnDefinition = "TEXT")
    private String ru;
}
