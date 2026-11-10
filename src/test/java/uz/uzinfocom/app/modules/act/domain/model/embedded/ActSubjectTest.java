package uz.uzinfocom.app.modules.act.domain.model.embedded;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

import static org.assertj.core.api.Assertions.assertThat;

class ActSubjectTest {

    @Test
    void legalEntityNeedsTinAndName() {
        assertThat(new ActSubject(SubjectType.LEGAL_ENTITY, "123456789", "MChJ Suv", null, null).isComplete()).isTrue();
        assertThat(new ActSubject(SubjectType.LEGAL_ENTITY, null, "MChJ Suv", null, "addr").isComplete()).isFalse();
        assertThat(new ActSubject(SubjectType.LEGAL_ENTITY, "123456789", " ", null, "addr").isComplete()).isFalse();
    }

    @Test
    void geographicPointAndPersonNeedActualAddress() {
        assertThat(new ActSubject(SubjectType.GEOGRAPHIC_POINT, null, null, null, "addr").isComplete()).isTrue();
        assertThat(new ActSubject(SubjectType.PHYSICAL_PERSON, null, null, null, null).isComplete()).isFalse();
    }

    @Test
    void typeIsRequired() {
        assertThat(new ActSubject(null, "123456789", "MChJ Suv", null, "addr").isComplete()).isFalse();
    }

    @Test
    void normalizeClearsLegalEntityFieldsForOtherTypes() {
        ActSubject subject = new ActSubject(SubjectType.PHYSICAL_PERSON, "123456789", "MChJ Suv", "legal", "addr");

        subject.normalize();

        assertThat(subject.getTin()).isNull();
        assertThat(subject.getName()).isNull();
        assertThat(subject.getLegalAddress()).isNull();
        assertThat(subject.getActualAddress()).isEqualTo("addr");
    }

    @Test
    void labelIsNameForLegalEntityOtherwiseAddress() {
        assertThat(new ActSubject(SubjectType.LEGAL_ENTITY, "123456789", "MChJ Suv", null, "addr").label())
                .isEqualTo("MChJ Suv");
        assertThat(new ActSubject(SubjectType.GEOGRAPHIC_POINT, null, null, null, "addr").label())
                .isEqualTo("addr");
    }
}
