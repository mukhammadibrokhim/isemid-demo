package uz.uzinfocom.app.orchestration.notification.application.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.orchestration.notification.application.dto.NotificationFilterRequest;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class NotificationSpecification {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    public Specification<Notification> byFilter(Long recipientUserId, NotificationFilterRequest filter) {
        Objects.requireNonNull(recipientUserId, "recipientUserId must not be null");
        Objects.requireNonNull(filter, "NotificationFilterRequest must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("recipientUserId"), recipientUserId));

            if (Boolean.TRUE.equals(filter.unreadOnly())) {
                predicates.add(cb.isFalse(root.get("read")));
            }

            List<?> types = normalize(filter.types());
            if (!types.isEmpty()) {
                predicates.add(root.get("type").in(types));
            }

            List<?> entityTypes = normalize(filter.entityTypes());
            if (!entityTypes.isEmpty()) {
                predicates.add(root.get("entityType").in(entityTypes));
            }

            if (filter.from() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("occurredAt"),
                                filter.from().atStartOfDay(APPLICATION_ZONE).toInstant()
                        )
                );
            }

            if (filter.to() != null) {
                Instant toExclusive = filter.to().plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant();
                predicates.add(cb.lessThan(root.get("occurredAt"), toExclusive));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static <T> List<T> normalize(List<T> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }
}
