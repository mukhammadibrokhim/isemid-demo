package uz.uzinfocom.isemid.features.report.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.patient.models.Address;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uz.uzinfocom.isemid.commons.utils.DateTimeUtil.formatDate;

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

            String birthStr = birthDate.format(DATE_FORMATTER);

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