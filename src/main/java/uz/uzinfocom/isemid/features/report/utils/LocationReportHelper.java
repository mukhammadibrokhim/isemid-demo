package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Address;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class LocationReportHelper {
    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public LocationReportDto buildRegionReports(List<Form058> forms) {
        var regions = catalogService.finAllByType(CatalogType.state).stream()
                .map(region -> LocationReportDto.ReportDto.builder()
                        .code(region.getCode())
                        .name(getLocalizedName(region))
                        .confirmed((int) countByStatus(forms, FormStatus.APPROVED, region.getCode(), "state"))
                        .unconfirmed((int) countByStatus(forms, null, region.getCode(), "state"))
                        .build()
                )
                .toList();

        return getLocationReportDto(forms, regions, "state", null);
    }

    public LocationReportDto buildDistrictReports(String parentCode, List<Form058> forms) {
        var districts = catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode).stream()
                .map(district -> LocationReportDto.ReportDto.builder()
                        .code(district.getCode())
                        .name(getLocalizedName(district))
                        .confirmed((int) countByStatus(forms, FormStatus.APPROVED, district.getCode(), "city"))
                        .unconfirmed((int) countByStatus(forms, null, district.getCode(), "city"))
                        .build()
                )
                .toList();

        return getLocationReportDto(forms, districts, "state", parentCode);
    }

    public LocationReportDto buildNeighborhoodReports(String parentCode, List<Form058> forms) {
        var neighborhoods = catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode).stream()
                .map(nbh -> LocationReportDto.ReportDto.builder()
                        .code(nbh.getCode())
                        .name(getLocalizedName(nbh))
                        .confirmed((int) countByStatus(forms, FormStatus.APPROVED, nbh.getCode(), "neighborhood"))
                        .unconfirmed((int) countByStatus(forms, null, nbh.getCode(), "neighborhood"))
                        .build()
                )
                .toList();

        return getLocationReportDto(forms, neighborhoods, "city", parentCode);
    }

    private String getLocalizedName(Catalog catalog) {
        var locale = LocaleContextHolder.getLocale().getLanguage();

        return switch (locale) {
            case "uz-Cyrl" -> catalog.getNameUzCyril();
            case "ru" -> catalog.getNameRu();
            case "kaa" -> catalog.getNameKaa();
            default -> catalog.getNameUz();
        };
    }

    private LocationReportDto getLocationReportDto(List<Form058> addrForms,
                                                   List<LocationReportDto.ReportDto> reports,
                                                   String level,
                                                   String parentCode) {

        var filtered = addrForms.stream()
                .filter(f -> {
                    var addr = getTemporaryAddress(f);
                    if (addr == null) return false;
                    return switch (level) {
                        case "state" -> parentCode == null || parentCode.equals(addr.getStateCode());
                        case "city" -> parentCode == null || parentCode.equals(addr.getCityCode());
                        case "neighborhood" -> parentCode == null || parentCode.equals(addr.getNeighborhoodCode());
                        default -> false;
                    };
                })
                .toList();

        long confirmed = filtered.stream().filter(f -> f.getStatus() == FormStatus.APPROVED).count();
        long unconfirmed = filtered.stream().filter(f -> f.getStatus() != FormStatus.APPROVED).count();

        return LocationReportDto.builder()
                .reports(reports)
                .totalConfirmed((int) confirmed)
                .totalUnconfirmed((int) unconfirmed)
                .totalDocuments(filtered.size())
                .build();
    }

    private Address getTemporaryAddress(Form058 form) {
        return form.getPatient().getAddresses().stream()
                .filter(addr -> addr.getType().equals(AddressType.TEMPORARY))
                .findFirst()
                .orElse(null);
    }

    private long countByStatus(List<Form058> forms, FormStatus status, String code, String level) {
        return forms.stream()
                .filter(f -> (status == null && f.getStatus() != FormStatus.APPROVED)
                        || (status != null && f.getStatus() == status))
                .map(this::getTemporaryAddress)
                .filter(addr -> addr != null && switch (level) {
                    case "state" -> code.equals(addr.getStateCode());
                    case "city" -> code.equals(addr.getCityCode());
                    case "neighborhood" -> code.equals(addr.getNeighborhoodCode());
                    default -> false;
                })
                .count();
    }

    public LocationReportDto buildByParentCode(String parentCode, List<Form058> forms) {
        var catalogs = catalogService.findAllByParentCode(parentCode);

        if (catalogs.isEmpty()) {
            throw new ApiException(NOT_FOUND, messageUtil.get("error.catalog.notfound", parentCode));
        }

        var distinctTypes = catalogs.stream()
                .map(Catalog::getType)
                .distinct()
                .toList();

        if (distinctTypes.size() > 1) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.catalog.mixed", parentCode, distinctTypes));
        }

        var catalogType = distinctTypes.getFirst();

        LocationReportDto report = switch (catalogType) {
            case "city" -> buildDistrictReports(parentCode, forms);
            case "neighborhood" -> buildNeighborhoodReports(parentCode, forms);
            default -> buildRegionReports(forms);
        };

//        report.setLast(isLeafByCatalogType(parentCode));
        return report;
    }

    public boolean isLeafByCatalogType(String parentCode) {
        Catalog catalog = catalogService.findByCode(parentCode);
        return switch (catalog.getType()) {
            case "neighborhood","city" -> true;
            default -> false;
        };
    }
}