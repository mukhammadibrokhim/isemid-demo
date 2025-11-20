package uz.uzinfocom.isemid.features.card.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.boot.model.naming.Identifier;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isHead;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class CardSpecification {
    public static Specification<Card> build(
            CardStatus cardStatus,
            String mkb10Code,
            Long formId,
            User currentUser
    ) {
        return (root, query, cb) -> {

            Objects.requireNonNull(query).distinct(true);

            Join<Card, Form058> formJoin = root.join("form058", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (cardStatus != null) {
                predicates.add(cb.equal(root.get("status"), cardStatus));
            }

            if (hasText(mkb10Code)) {
                predicates.add(
                        cb.like(
                                cb.lower(formJoin.get("mkb10Code")),
                                "%" + mkb10Code.toLowerCase() + "%"
                        )
                );
            }

            if (formId != null) {
                predicates.add(cb.equal(formJoin.get("id"), formId));
            }

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {

                var org = currentUser.getCurrentOrganization();

                Join<Form058, ?> createdOrgJoin =
                        formJoin.join("createdOrg", JoinType.LEFT);

                Predicate levelPredicate =
                        SpecificationUtils.buildLevelPredicate(
                                createdOrgJoin,
                                cb,
                                org.getLevelType(),
                                org.getStateCode(),
                                org.getCityCode()
                        );

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
            String patientDocVal,
            User currentUser
    ) {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            boolean isHead = isHead(currentUser);

            Long orgId = null;
            Long userId = null;

            if (isHead && currentUser.getCurrentOrganization() != null) {
                orgId = currentUser.getCurrentOrganization().getId();
            } else {
                userId = currentUser.getId();
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

            if (hasText(patientDocVal)) {

                Join<Card, Form058> formJoin = root.join("form058", JoinType.LEFT);
                Join<Form058, Patient> patientJoin = formJoin.join("patient", JoinType.LEFT);
                Join<Patient, Identifier> idJoin = patientJoin.join("identifiers", JoinType.LEFT);

                predicates.add(cb.equal(idJoin.get("value"), patientDocVal));
            }

            if (!isSuperAdmin(currentUser)) {
                if (isHead && orgId != null) {
                    predicates.add(cb.equal(root.get("createdOrg").get("id"), orgId));
                } else if (userId != null) {
                    predicates.add(cb.equal(root.join("users", JoinType.LEFT).get("id"), userId));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
