package uz.uzinfocom.isemid.features.act.models.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ActSubjectInfo {

    /**
     * Reference/catalog code:
     * LEGAL_ENTITY / GEOGRAPHIC_POINT / PHYSICAL_PERSON
     */
    @Column(name = "subject_type_code", length = 50)
    private String subjectTypeCode;

    /**
     * Only for LEGAL_ENTITY.
     */
    @Column(name = "subject_tin", length = 20)
    private String subjectTin;

    /**
     * Organization name. Only for LEGAL_ENTITY.
     */
    @Column(name = "subject_legal_name", length = 500)
    private String subjectLegalName;

    /**
     * Legal address. Only for LEGAL_ENTITY.
     */
    @Column(name = "subject_legal_address", length = 1000)
    private String subjectLegalAddress;

    /**
     * Actual address. Required for all subject types.
     */
    @Column(name = "subject_actual_address", length = 1000)
    private String subjectActualAddress;
}