package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 50)
    private SubjectType subjectType;

    @Column(name = "tin")
    private Integer tin;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "institution_address")
    private String institutionAddress;

    @Column(name = "institution_legal_address")
    private String institutionLegalAddress;

    /**
     * Legal-entity-only fields are meaningless once the subject turns out to
     * be a geographic point or a physical person — this clears them instead
     * of leaving stale data behind after the subject type changes.
     */
    public void normalize() {
        if (subjectType == null) {
            return;
        }
        if (subjectType != SubjectType.LEGAL_ENTITY) {
            tin = null;
            institutionName = null;
            institutionLegalAddress = null;
        }
    }
}
