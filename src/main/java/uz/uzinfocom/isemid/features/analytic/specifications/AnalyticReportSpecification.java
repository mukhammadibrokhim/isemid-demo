package uz.uzinfocom.isemid.features.analytic.specifications;


import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticReportSpecification {

    public static Specification<AnalyticReport> build(
            LocalDate fromDate,
            LocalDate toDate,
            String regionCode,
            String mkb10Code,
            User user
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔹 1. Sana oralig‘i bo‘yicha filter (umumiy util)
            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            // 🔹 2. Foydalanuvchi cheklovi
            if (user != null && user.getCurrentOrganization() != null) {
                Organization org = user.getCurrentOrganization();

                if (org.getLevelType() == LevelType.REPUBLICAN) {
                    // Respublika — barcha hisobotlarni ko‘ra oladi, hech qanday cheklov qo‘shilmaydi
                } else {
                    // Oddiy foydalanuvchi — faqat o‘zinikini ko‘radi
                    predicates.add(cb.equal(root.get("createdBy"), user.getId()));
                }
            } else {
                // Tizimda organization bo‘lmasa ham — faqat o‘zinikini ko‘rsin
                predicates.add(cb.equal(root.get("createdBy"), user.getId()));
            }

            // 🔹 3. Region / MKB10 bo‘yicha filterlar (ixtiyoriy)
            if (regionCode != null && !regionCode.isEmpty()) {
//                predicates.add(cb.equal(root.get("regionCode"), regionCode));
            }
            if (mkb10Code != null && !mkb10Code.isEmpty()) {
                predicates.add(cb.equal(root.get("mkb10Code"), mkb10Code));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

