package uz.uzinfocom.app.modules.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One identifier the IAM provider knows an organization by (soliq/TAX, argos,
 * dhp, ...), keyed by its {@code system} URI.
 */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrganizationIdentifier {

    @Column(name = "system", nullable = false)
    private String system;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "type_code", length = 50)
    private String typeCode;
}
