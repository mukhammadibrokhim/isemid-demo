package uz.uzinfocom.app.platform.settings.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingFilterRequest;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SystemSettingSpecification {

    private SystemSettingSpecification() {
    }

    public static Specification<SystemSetting> byFilter(SystemSettingFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (StringUtils.hasText(request.search())) {
                String search = "%" + request.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("settingKey")), search));
            }

            if (request.valueType() != null) {
                predicates.add(cb.equal(root.get("valueType"), request.valueType()));
            }

            if (request.active() != null) {
                predicates.add(cb.equal(root.get("active"), request.active()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
