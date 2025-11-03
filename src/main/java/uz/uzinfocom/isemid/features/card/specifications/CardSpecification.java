package uz.uzinfocom.isemid.features.card.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isHead;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class CardSpecification {
    public static Specification<Card> build(
            CardStatus cardStatus,
            String mkb10Code,
            User currentUser
    ) {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            Join<Card, Form058> formJoin = root.join("form058", JoinType.LEFT);

            List<Predicate> predicates = Stream.of(
                    cardStatus != null ? cb.equal(root.get("status"), cardStatus) : null,
                    hasText(mkb10Code) ? cb.like(cb.lower(formJoin.get("mkb10Code")), "%" + mkb10Code.toLowerCase() + "%") : null
            ).filter(Objects::nonNull).toList();

            if (uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin(currentUser)) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                var org = currentUser.getCurrentOrganization();

                Join<Form058, ?> createdOrgJoin = formJoin.join("createdOrg", JoinType.LEFT);

                Predicate levelPredicate = uz.uzinfocom.isemid.commons.utils.SpecificationUtils.buildLevelPredicate(
                        createdOrgJoin, cb,
                        org.getLevelType(),
                        org.getStateCode(),
                        org.getCityCode()
                );

                predicates = new java.util.ArrayList<>(predicates);
                predicates.add(levelPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Card> build(
            LocalDate fromDate,
            LocalDate toDate,
            CardType cardType,
            CardStatus cardStatus,
            String mkb10Code,
            User currentUser
    ) {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            boolean isHead = isHead(currentUser);
            boolean isSuperAdmin = isSuperAdmin(currentUser);

            Long orgId = null;
            Long userId = null;

            if (isHead && currentUser.getCurrentOrganization() != null) {
                orgId = currentUser.getCurrentOrganization().getId();
            } else {
                userId = currentUser.getId();
            }

            if (isSuperAdmin) {
                return cb.conjunction();
            }
            if (fromDate != null || toDate != null) {
                SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");
            }
            if (cardType != null) {
                predicates.add(cb.equal(root.get("cardType"), cardType));
            }

            if (cardStatus != null) {
                predicates.add(cb.equal(root.get("status"), cardStatus));
            }

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("form058").get("finalMkb10Code"), mkb10Code.trim()));
            }

            if (isHead && orgId != null) {
                predicates.add(cb.equal(root.get("createdOrg").get("id"), orgId));
            } else if (!isHead && userId != null) {
                predicates.add(cb.equal(root.join("users", JoinType.LEFT).get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
