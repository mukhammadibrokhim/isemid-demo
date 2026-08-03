package uz.uzinfocom.isemid.features.report.job;

import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

/**
 * Cache/fayl kalitini quradi: filterType + filtrlar + tashkilot bo'yicha noyob bo'lishi kerak,
 * aks holda turli tashkilot/sana oralig'i so'ragan foydalanuvchilar bir-birining faylini ko'rib qoladi.
 */
public final class Form058ExportJobKeyBuilder {

    private Form058ExportJobKeyBuilder() {
    }

    public static String build(FilterType filterType, LocalDate fromDate, LocalDate toDate,
                               String stateCode, String cityCode, User user) {
        Long orgId = user.getCurrentOrganization() != null ? user.getCurrentOrganization().getId() : null;
        String raw = String.join("_",
                filterType.name(),
                String.valueOf(fromDate),
                String.valueOf(toDate),
                String.valueOf(stateCode),
                String.valueOf(cityCode),
                String.valueOf(orgId));
        return raw.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
