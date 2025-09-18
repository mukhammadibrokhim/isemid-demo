package uz.uzinfocom.isemid.features.card.specifications;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class CardSpecification {
    public static Specification<Card> build(
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            Long userId,
            boolean attachedUser,
            Long formId
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                        hasText(formTypeCode) ? criteriaBuilder.equal(root.get("cardType"), formTypeCode) : null,
                        formId != null ? criteriaBuilder.equal(root.join("form058", JoinType.LEFT).get("id"), formId) : null,
                        hasText(fromOrgName) ? criteriaBuilder.equal(root.get("form058").get("senderOrganization").get("name"), fromOrgName) : null,
                        hasText(toOrgName) ? criteriaBuilder.equal(root.get("form058").get("receiverOrganization").get("name"), toOrgName) : null,
                        cardStatus != null ? criteriaBuilder.equal(root.get("cardStatus"), cardStatus) : null,
                        (attachedUser && userId != null) ? criteriaBuilder.equal(root.get("users").get("id"), userId) : null
                ).filter(Objects::nonNull).toArray(Predicate[]::new)
        );
    }

    public static Specification<Card> build(Long userId, Long orgId, boolean isHead) {
        return (root, query, cb) -> cb.and(
                Stream.of(
                                (isHead && orgId != null) ? cb.equal(root.get("createdOrg").get("id"), orgId) : null,
                                (!isHead && userId != null) ? cb.equal(root.join("users").get("id"), userId) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }

}
