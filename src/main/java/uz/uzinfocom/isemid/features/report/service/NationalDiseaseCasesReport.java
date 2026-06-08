package uz.uzinfocom.isemid.features.report.service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.impls.CatalogServiceImpl;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
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
  private final MessageUtil messageUtil;

  @Override
  public LocationReportDto getByLocation(String mkb10Code, LocalDate fromDate, LocalDate toDate,
      String parentCode, User currentUser) {
    /*user's active data for filtering report*/
    Organization userCurrentOrganization = currentUser.getCurrentOrganization();
    LevelType userOrganizationLevelType = userCurrentOrganization.getLevelType();
    String userCurrentOrganizationStateCode = userCurrentOrganization.getStateCode();
    String userCurrentOrganizationCityCode = userCurrentOrganization.getCityCode();
    MedicalType currentOrganizationMedicalType = userCurrentOrganization.getMedicalType();


    List<Catalog> catalogs;
    // if parent code is not null and not empty then
    if (parentCode != null && !parentCode.isBlank()) {
    /*
       search according to parentCode from catalogue
    * */
      catalogs = catalogService.findAllByParentCode(parentCode);
      if (catalogs.isEmpty()) {
        throw new ApiException(NOT_FOUND, messageUtil.get("error.catalog.notfound", parentCode));
      }

      var distinctTypes = catalogs.stream()
          .map(Catalog::getType)
          .distinct()
          .toList();

      if (distinctTypes.size() > 1) {
        throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.catalog.mixed",
            parentCode, distinctTypes));
      }

      var catalogType = distinctTypes.getFirst();

      switch (catalogType) {
        case "city" -> catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode);
        case "neighborhood" ->
            catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode);
        default -> catalogService.finAllByType(CatalogType.state);
      };
    }//end if
    else{
      catalogs=catalogService.findByTypeAndCodeLike(CatalogType.state,"UZ-");

      var distinctTypes = catalogs.stream()
          .map(Catalog::getType)
          .distinct()
          .toList();

      if (distinctTypes.size() > 1) {
        throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.catalog.mixed",
            parentCode, distinctTypes));
      }

      var catalogType = distinctTypes.getFirst();
    }



    // 2.
    List<LocationReportRawData> rawData = nationalDiseaseReportRepository.getLocationReport(mkb10Code,
        fromDate, toDate, parentCode, userOrganizationLevelType, userCurrentOrganizationStateCode,
        userCurrentOrganizationCityCode);

    return getLocationReportDto(rawData, catalogs, userOrganizationLevelType);
  }

  private LocationReportDto getLocationReportDto(List<LocationReportRawData> rawData,
      List<Catalog> catalogs, LevelType level) {
    Map<String, LocationReportRawData> locationCodeRawDataMap = rawData.stream()
        .collect(Collectors.toMap(
            LocationReportRawData::code,
            Function.identity()
        ));

    // 3. Merge — every catalog entry gets a row, zero if no matching forms
    List<ReportDto> reports =
        catalogs.stream()
            .map(catalog -> {
              LocationReportRawData data = locationCodeRawDataMap.get(catalog.getCode());

              return ReportDto.builder()
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
