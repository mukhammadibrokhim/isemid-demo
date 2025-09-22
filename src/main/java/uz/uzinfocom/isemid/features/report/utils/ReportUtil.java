package uz.uzinfocom.isemid.features.report.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ReportUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static String resolveAge(LocalDate birthDate, Integer ageYears, Integer ageMonths) {
        if (birthDate != null) {
            Period period = Period.between(birthDate, LocalDate.now());
            int years = period.getYears();
            int months = period.getMonths();
            int days = period.getDays();

            if (years == 0) {
                // 1 yoshga yetmagan chaqaloq
                StringBuilder sb = new StringBuilder();
                if (months > 0) sb.append(months).append(" ой ");
                if (days > 0) sb.append(days).append(" кун");
                return sb.toString().trim() + "лик";
            } else if (years < 7) {
                // 7 yoshgacha bo'lgan bola
                StringBuilder sb = new StringBuilder();
                sb.append(years).append(" йил");
                if (months > 0) sb.append(" ").append(months).append(" ой");
                return sb.toString().trim() + "лик";
            } else {
                // 7 yoshdan katta bo‘lsa -> tug‘ilgan sanasi
                return birthDate.format(DATE_FORMATTER);
            }
        } else {
            // Agar faqat yil / oy berilgan bo‘lsa
            if (ageYears != null && ageYears < 7) {
                StringBuilder sb = new StringBuilder();
                sb.append(ageYears).append(" йил ");
                if (ageMonths != null && ageMonths > 0) {
                    sb.append(ageMonths).append(" ой");
                }
                return sb.toString().trim() + "лик";
            } else if (ageYears != null && ageYears >= 7) {
                // Faqat yil bo‘lsa va 7 yoshdan katta
                return ageYears + " йил";
            } else if (ageMonths != null) {
                // 1 yoshga yetmagan va faqat oy berilgan
                return ageMonths + " ойлик";
            }
            return "";
        }
    }

    public class AddressUtil {

        public static String resolvePermanentAddress(Form058 form, CatalogService catalogService) {
            if (form == null || form.getPatient() == null || form.getPatient().getAddresses() == null) {
                return "";
            }

            return form.getPatient().getAddresses().stream()
                    .filter(a -> AddressType.PERMANENT.equals(a.getType()))
                    .findFirst()
                    .map(a -> Stream.of(
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
                            .collect(Collectors.joining(", ")))
                    .orElse("");
        }
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

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? DATE_TIME_FORMATTER.format(dateTime) : "";
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? DATE_FORMATTER.format(dateTime) : "";
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
            createdDate = DATE_TIME_FORMATTER.format(form.getCreatedAt().toLocalDateTime());
        }

        return Stream.of(orgName, notifierName, createdDate)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }
}