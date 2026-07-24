package uz.uzinfocom.isemid.features.organization.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.TextNormalizationUtils;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class OrganizationSpecification {

    public static Specification<Organization> build(
            String name,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<>(Stream.of(
                    criteriaBuilder.isNull(root.get("parent")),
                    hasText(name) ? criteriaBuilder.like(
                            criteriaBuilder.function(
                                    "translate", String.class,
                                    criteriaBuilder.lower(root.get("name")),
                                    criteriaBuilder.literal(TextNormalizationUtils.APOSTROPHE_CHARS),
                                    criteriaBuilder.literal("")
                            ),
                            "%" + TextNormalizationUtils.stripApostrophes(name.toLowerCase()) + "%"
                    ) : null,
                    hasText(stateCode) ? criteriaBuilder.equal(root.get("stateCode"), stateCode) : null,
                    hasText(cityCode) ? criteriaBuilder.equal(root.get("cityCode"), cityCode) : null
            ).filter(Objects::nonNull).toList());

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                Organization currOrgan = currentUser.getCurrentOrganization();
                LevelType level = currOrgan.getLevelType();

                if (isSuperAdmin(currentUser)) {
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
                Predicate levelPredicate = switch (level) {
                    case REPUBLICAN -> criteriaBuilder.conjunction();
                    case REGIONAL -> criteriaBuilder.equal(root.get("stateCode"), currOrgan.getStateCode());
                    case URBAN -> {
                        if ("UZ-TK".equalsIgnoreCase(currOrgan.getStateCode())) {
                            yield criteriaBuilder.equal(root.get("stateCode"), currOrgan.getStateCode());
                        } else {
                            yield criteriaBuilder.equal(root.get("cityCode"), currOrgan.getCityCode());
                        }
                    }
                    case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                            criteriaBuilder.equal(root.get("id"), currOrgan.getId());
                };
                predicates.add(levelPredicate);

            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
