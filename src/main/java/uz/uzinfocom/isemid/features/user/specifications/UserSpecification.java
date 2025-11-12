package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class UserSpecification {
    public static Specification<User> build(
            final String nnuzb,
            final String firstName,
            final String lastName,
            final String middleName,
            final String phoneNumber,
            final String accessLevelCode,
            final Boolean active,
            User currentUser
    ) {
        return (root, query, cb) -> {
            var predicates = Stream.of(
                    hasText(nnuzb) ? cb.equal(root.get("nnuzb"), nnuzb) : null,
                    hasText(firstName) ? cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%") : null,
                    hasText(lastName) ? cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%") : null,
                    hasText(middleName) ? cb.like(cb.lower(root.get("middleName")), "%" + middleName.toLowerCase() + "%") : null,
                    hasText(phoneNumber) ? cb.equal(root.get("phoneNumber"), phoneNumber) : null,
                    hasText(accessLevelCode)
                            ? cb.equal(root.join("accessLevel", JoinType.LEFT).get("code"), accessLevelCode)
                            : null,
                    active != null ? cb.equal(root.get("active"), active) : null
            ).filter(Objects::nonNull).collect(Collectors.toList());

            if (currentUser != null && !isSuperAdmin(currentUser)) {
                List<Organization> userOrgs = currentUser.getOrganizations();
                if (userOrgs != null && !userOrgs.isEmpty()) {
                    Join<User, Organization> orgJoin = root.join("organizations", JoinType.LEFT);

                    List<Predicate> orgPredicates = new ArrayList<>();

                    for (Organization org : userOrgs) {
                        LevelType level = org.getLevelType();
                        if (level == null) continue;

                        Predicate orgPredicate = switch (level) {
                            case REPUBLICAN -> cb.conjunction();
                            case REGIONAL -> cb.equal(orgJoin.get("stateCode"), org.getStateCode());
                            case URBAN -> {
                                if ("UZ-TK".equalsIgnoreCase(org.getStateCode())) {
                                    yield cb.equal(orgJoin.get("stateCode"), org.getStateCode());
                                } else {
                                    yield cb.equal(orgJoin.get("cityCode"), org.getCityCode());
                                }
                            }
                            case DISTRICT, AREA, INTERDISTRICT -> cb.equal(orgJoin.get("id"), org.getId());
                            default -> cb.disjunction();
                        };
                        orgPredicates.add(orgPredicate);
                    }

                    if (!orgPredicates.isEmpty()) {
                        predicates.add(cb.or(orgPredicates.toArray(new Predicate[0])));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}