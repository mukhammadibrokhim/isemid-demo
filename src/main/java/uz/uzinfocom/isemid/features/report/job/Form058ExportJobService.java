package uz.uzinfocom.isemid.features.report.job;

import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form058ExportJobService {

    /**
     * Berilgan filtrlar uchun mavjud jobni qaytaradi, agar mavjud bo'lmasa (yoki
     * avvalgisi FAILED bo'lsa) yangisini fon jarayonida boshlab, uni qaytaradi.
     */
    Form058ExportJob getOrStart(FilterType filterType, LocalDate fromDate, LocalDate toDate,
                                String stateCode, String cityCode, User user);
}
