package uz.uzinfocom.app.modules.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A raw classifier code the IAM provider assigned to an organization's
 * {@code type} (e.g. DHP subordination group {@code I_1}, structure
 * {@code 999}). Kept verbatim next to the derived enums so the mapping can be
 * refined later without re-syncing.
 */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrganizationTypeCoding {

    @Column(name = "system", nullable = false)
    private String system;

    @Column(name = "code", nullable = false, length = 100)
    private String code;
}
