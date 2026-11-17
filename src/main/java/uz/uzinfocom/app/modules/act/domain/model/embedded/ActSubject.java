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

/**
 * Who or what the act was drawn up about — the form's «Tashkilot turi»
 * block. Which fields apply depends on {@link #type}:
 * <ul>
 *   <li>{@link SubjectType#LEGAL_ENTITY} — {@link #tin} (looked up via
 *       API2), {@link #name}, {@link #legalAddress}, {@link #actualAddress}</li>
 *   <li>{@link SubjectType#GEOGRAPHIC_POINT} / {@link SubjectType#PHYSICAL_PERSON}
 *       — {@link #actualAddress} only</li>
 * </ul>
 * Column names are the pre-rename {@code institution_*} ones, kept so the
 * legacy migration scripts and existing data stay untouched.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ActSubject {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 50)
    private SubjectType type;

    @Column(name = "tin", length = 20)
    private String tin;

    @Column(name = "institution_name")
    private String name;

    @Column(name = "institution_legal_address")
    private String legalAddress;

    @Column(name = "institution_address")
    private String actualAddress;

    /**
     * Legal-entity-only fields are meaningless once the subject turns out to
     * be a geographic point or a physical person — this clears them instead
     * of leaving stale data behind after the subject type changes.
     */
    public void normalize() {
        if (type == null) {
            return;
        }
        if (type != SubjectType.LEGAL_ENTITY) {
            tin = null;
            name = null;
            legalAddress = null;
        }
    }

    /**
     * Whether every field the current {@link #type} requires is filled in.
     * Not enforced on save (acts are saved as drafts any number of times),
     * only once the act is marked ready.
     */
    public boolean isComplete() {
        if (type == null) {
            return false;
        }
        if (type == SubjectType.LEGAL_ENTITY) {
            return hasText(tin) && hasText(name);
        }
        return hasText(actualAddress);
    }

    /**
     * One-line label for list views: the organization's name for a legal
     * entity, otherwise the address.
     */
    public String label() {
        return type == SubjectType.LEGAL_ENTITY && hasText(name) ? name : actualAddress;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
