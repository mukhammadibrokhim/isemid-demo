package uz.uzinfocom.isemid.features.report.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.patient.models.Address;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uz.uzinfocom.isemid.commons.utils.DateTimeUtil.formatDate;
import static uz.uzinfocom.isemid.commons.utils.DateTimeUtil.formatDateTime;

@UtilityClass
public class ReportUtil {

    public static String resolveAge(LocalDate birthDate, Integer ageYears, Integer ageMonths) {
        if (birthDate != null) {
            Period period = Period.between(birthDate, LocalDate.now());
            int years = period.getYears();
            int months = period.getMonths();
            int days = period.getDays();

            String birthStr = formatDate(birthDate);

            if (years == 0) {
                // 1 yoshga yetmagan chaqaloq: oy va kun
                StringBuilder sb = new StringBuilder();
                if (months > 0) sb.append(months).append(" ой ");
                if (days > 0) sb.append(days).append(" кун");
                return birthStr + " — " + sb.toString().trim() + "лик";
            } else if (years < 7) {
                // 7 yoshgacha: yil va oy
                StringBuilder sb = new StringBuilder();
                sb.append(years).append(" йил");
                if (months > 0) sb.append(" ").append(months).append(" ой");
                return birthStr + " — " + sb.toString().trim() + "лик";
            } else {
                // 7 yoshdan katta: faqat yosh
                return birthStr + " — " + years + " ёш";
            }
        } else {
            // Agar faqat yil / oy berilgan bo‘lsa (birthDate yo‘q)
            if (ageYears != null) {
                if (ageYears < 7) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ageYears).append(" йил");
                    if (ageMonths != null && ageMonths > 0) {
                        sb.append(" ").append(ageMonths).append(" ой");
                    }
                    return sb.toString().trim() + "лик";
                } else {
                    return ageYears + " ёш";
                }
            } else if (ageMonths != null) {
                // 1 yoshga yetmagan va faqat oy berilgan
                return ageMonths + " ойлик";
            }
            return "";
        }
    }

    public static String resolveAddresses(Form058 form, CatalogService catalogService) {
        if (form == null || form.getPatient() == null || form.getPatient().getAddresses() == null) {
            return "";
        }

        String permanent = form.getPatient().getAddresses().stream()
                .filter(a -> AddressType.PERMANENT.equals(a.getType()))
                .findFirst()
                .map(a -> formatAddress(a, catalogService))
                .orElse("");

        String temporary = form.getPatient().getAddresses().stream()
                .filter(a -> AddressType.TEMPORARY.equals(a.getType()))
                .findFirst()
                .map(a -> formatAddress(a, catalogService))
                .orElse("");

        StringBuilder sb = new StringBuilder();
        if (!permanent.isBlank()) {
            sb.append("Доимий: ").append(permanent).append(".");
        }
        if (!temporary.isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append("Яшаш: ").append(temporary).append(".");
        }
        return sb.toString();
    }

    private static String formatAddress(Address a, CatalogService catalogService) {
        return Stream.of(
                        catalogService.findByTypeAndCode(CatalogType.state, a.getStateCode())
                                .map(Catalog::getNameUzCyril).orElse(""),
                        catalogService.findByTypeAndCode(CatalogType.city, a.getCityCode())
                                .map(Catalog::getNameUzCyril).orElse(""),
                        catalogService.findByTypeAndCode(CatalogType.neighborhood, a.getNeighborhoodCode())
                                .map(Catalog::getNameUzCyril).orElse(""),
                        a.getHouseNumber(),
                        a.getApartmentNumber()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    /**
     * N+1 dan qochish uchun: {@code catalogService} orqali har manzil uchun DB'ga
     * murojaat qilmasdan, oldindan yuklab olingan {@code catalogCache}
     * (key = "{CatalogType}:{code}", value = nomi) dan foydalanadi.
     */
    public static String resolveAddressesV2(Patient patient, Map<String, String> catalogCache) {
        if (patient == null || patient.getAddresses() == null) {
            return "";
        }

        String permanent = patient.getAddresses().stream()
                .filter(a -> AddressType.PERMANENT.equals(a.getType()))
                .findFirst()
                .map(a -> formatAddressV2(a, catalogCache))
                .orElse("");

        String temporary = patient.getAddresses().stream()
                .filter(a -> AddressType.TEMPORARY.equals(a.getType()))
                .findFirst()
                .map(a -> formatAddressV2(a, catalogCache))
                .orElse("");

        StringBuilder sb = new StringBuilder();
        if (!permanent.isBlank()) {
            sb.append("Доимий: ").append(permanent).append(".");
        }
        if (!temporary.isBlank()) {
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append("Яшаш: ").append(temporary).append(".");
        }
        return sb.toString();
    }

    public static String catalogCacheKey(CatalogType type, String code) {
        return type.name() + ":" + code;
    }

    private static String formatAddressV2(Address a, Map<String, String> catalogCache) {
        return Stream.of(
                        catalogCache.getOrDefault(catalogCacheKey(CatalogType.state, a.getStateCode()), ""),
                        catalogCache.getOrDefault(catalogCacheKey(CatalogType.city, a.getCityCode()), ""),
                        catalogCache.getOrDefault(catalogCacheKey(CatalogType.neighborhood, a.getNeighborhoodCode()), ""),
                        a.getHouseNumber(),
                        a.getApartmentNumber()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    public static String resolveHospitalAndDate(
            Form058 form,
            OrganizationRepository organizationRepository
    ) {
        if (form == null) {
            return "";
        }

        String hospitalName = Optional.ofNullable(form.getHospitalPlaceId())
                .flatMap(organizationRepository::findById)
                .map(Organization::getName)
                .orElse("");

        String admissionDate = form.getAdmissionDate() != null
                ? formatDate(form.getAdmissionDate())
                : "";

        return hospitalName + (admissionDate.isBlank() ? "" : ", " + admissionDate);
    }

    /**
     * N+1 dan qochish uchun: har qator uchun {@code organizationRepository.findById}
     * chaqirish o'rniga, oldindan batch bilan yuklab olingan {@code orgByIdCache} dan foydalanadi.
     */
    public static String resolveHospitalAndDateV2(
            Form058 form,
            Map<Long, Organization> orgByIdCache
    ) {
        if (form == null) {
            return "";
        }

        String hospitalName = Optional.ofNullable(form.getHospitalPlaceId())
                .map(orgByIdCache::get)
                .map(Organization::getName)
                .orElse("");

        String admissionDate = form.getAdmissionDate() != null
                ? formatDate(form.getAdmissionDate())
                : "";

        return hospitalName + (admissionDate.isBlank() ? "" : ", " + admissionDate);
    }

    public static String resolveOrganizationName(
            Long organizationId,
            OrganizationRepository organizationRepository
    ) {
        if (organizationId == null) {
            return "";
        }

        return organizationRepository.findById(organizationId)
                .map(Organization::getName)
                .orElse("");
    }

    public static String resolveOrganizationNameV2(
            Long organizationId,
            Map<Long, Organization> orgByIdCache
    ) {
        if (organizationId == null) {
            return "";
        }

        return Optional.ofNullable(orgByIdCache.get(organizationId))
                .map(Organization::getName)
                .orElse("");
    }

    public static String resolveCreatedOrgAndDocName(Form058 form) {
        if (form == null) return "";

        String orgName = Optional.ofNullable(form.getCreatedOrg())
                .map(Organization::getName)
                .orElse("");

        String notifierName = form.getNotifierFullName() != null
                ? form.getNotifierFullName()
                : "";

        String createdDate = "";
        if (form.getCreatedAt() != null) {
            createdDate = formatDateTime(form.getCreatedAt());
        }

        return Stream.of(orgName, notifierName, createdDate)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    /**
     * N+1 dan qochish uchun: {@code form.getCreatedOrg()} (lazy proxy, har chaqiruvda
     * DB'ga boradi) o'rniga, {@code form.getCreatedOrgUuid()} allaqachon oddiy ustun
     * sifatida yuklangan, shuning uchun uni batch bilan olingan {@code orgByUuidCache}
     * bilan solishtiramiz.
     */
    public static String resolveCreatedOrgAndDocNameV2(Form058 form, Map<UUID, Organization> orgByUuidCache) {
        if (form == null) return "";

        String orgName = Optional.ofNullable(form.getCreatedOrgUuid())
                .map(orgByUuidCache::get)
                .map(Organization::getName)
                .orElse("");

        String notifierName = form.getNotifierFullName() != null
                ? form.getNotifierFullName()
                : "";

        String createdDate = "";
        if (form.getCreatedAt() != null) {
            createdDate = formatDateTime(form.getCreatedAt());
        }

        return Stream.of(orgName, notifierName, createdDate)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private static final Map<Integer, String> uzMonthsCyrl = Map.ofEntries(
            Map.entry(1, "январь"),
            Map.entry(2, "февраль"),
            Map.entry(3, "март"),
            Map.entry(4, "апрель"),
            Map.entry(5, "май"),
            Map.entry(6, "июнь"),
            Map.entry(7, "июль"),
            Map.entry(8, "август"),
            Map.entry(9, "сентябрь"),
            Map.entry(10, "октябрь"),
            Map.entry(11, "ноябрь"),
            Map.entry(12, "декабрь")
    );

    private String getUzMonthCyril(LocalDate date) {
        return (date != null) ? uzMonthsCyrl.get(date.getMonthValue()) : "________";
    }

    public static String buildDateText(LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter yearFmt = DateTimeFormatter.ofPattern("yyyy");

        // fromDate
        String fromDay = (fromDate != null) ? fromDate.format(dayFmt) : "____";
        String fromMonth = getUzMonthCyril(fromDate);
        String fromYear = (fromDate != null) ? fromDate.format(yearFmt) : "20__";

        // toDate
        String toDay = (toDate != null) ? toDate.format(dayFmt) : "____";
        String toMonth = getUzMonthCyril(toDate);
        String toYear = (toDate != null) ? toDate.format(yearFmt) : "20__";

        return "Бошланган сана «" + fromDay + "» " + fromMonth + " " + fromYear + " йил   " +
                "Тугатилган сана «" + toDay + "» " + toMonth + " " + toYear + " йил";
    }

}