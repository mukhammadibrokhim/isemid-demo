package uz.uzinfocom.isemid.features.report.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.impls.CatalogServiceImpl;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto.ReportDto;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRawData;
import uz.uzinfocom.isemid.features.report.repository.NationalDiseaseReportRepository;
import uz.uzinfocom.isemid.features.user.models.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class NationalDiseaseCasesReport implements LocationReportService {

  private final NationalDiseaseReportRepository nationalDiseaseReportRepository;
  private final CatalogServiceImpl catalogService;

  @Override
  public LocationReportDto getByLocation(String mkb10Code, LocalDate fromDate, LocalDate toDate,
      LevelType level, String parentCode, User currentUser) {
    Organization organization = currentUser.getCurrentOrganization();
    // 1. All catalog entries — guarantees zero-count rows appear
    List<Catalog> catalogs = resolveCatalogs(level, parentCode);
    // 2.
    List<LocationReportRawData> rawData = nationalDiseaseReportRepository.getLocationReport(mkb10Code,
        fromDate, toDate, parentCode, organization);

    Map<String, LocationReportRawData> locationCodeRawDataMap = rawData.stream()
        .collect(Collectors.toMap(
            LocationReportRawData::code,
            Function.identity()
        ));

    // 3. Merge — every catalog entry gets a row, zero if no matching forms
    List<LocationReportDto.ReportDto> reports =
        catalogs.stream()
            .map(catalog -> {
              LocationReportRawData data = locationCodeRawDataMap.get(catalog.getCode());

              return LocationReportDto.ReportDto.builder()
                  .code(catalog.getCode())
                  .name(getLocalizedName(catalog))
                  .confirmed(data == null ? 0 : data.confirmed())
                  .unconfirmed(data == null ? 0 : data.unconfirmed())
                  .build();
            })
            .toList();

    int totalConfirmed = reports.stream()
        .mapToInt(ReportDto::getConfirmed)
        .sum();

    int totalUnconfirmed = reports.stream()
        .mapToInt(ReportDto::getUnconfirmed)
        .sum();


    return LocationReportDto.builder()
        .reports(reports)
        .totalConfirmed(totalConfirmed)
        .totalUnconfirmed(totalUnconfirmed)
        .totalDocuments(totalConfirmed + totalUnconfirmed)
        .level(level.numericForm())
        .build();
  }

  private List<Catalog> resolveCatalogs(LevelType level, String parentCode) {
    return switch (level) {
      case REPUBLICAN                         -> catalogService.findAllByType(CatalogType.state);
      case REGIONAL, URBAN                    -> catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode);
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode);
    };
  }

  private String getLocalizedName(Catalog catalog) {
    String locale = LocaleContextHolder.getLocale().getLanguage();
    return switch (locale) {
      case "uz-Cyrl" -> catalog.getNameUzCyril();
      case "ru"      -> catalog.getNameRu();
      case "kaa"     -> catalog.getNameKaa();
      default        -> catalog.getNameUz();
    };
  }
}
