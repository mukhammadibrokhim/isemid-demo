package uz.uzinfocom.isemid.features.report.service.impls;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.CaseCountDynamicsData;
import uz.uzinfocom.isemid.features.report.dtos.request.DiseaseFilterRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.MonitoringDynamicsResponse;
import uz.uzinfocom.isemid.features.report.repository.RegionalDiseaseDashboardRepository;
import uz.uzinfocom.isemid.features.report.service.RegionalDiseaseDashboardService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionalDiseaseDashboardServiceImpl implements RegionalDiseaseDashboardService {

  private final RegionalDiseaseDashboardRepository regionalDiseaseDashboardRepository;

  @Override
  public MonitoringDynamicsResponse getMonitoring(LocalDate fromDate, LocalDate toDate,
      String regionCode, String mkb10Code) {
    DiseaseFilterRequest diseaseFilterRequest = new DiseaseFilterRequest(fromDate, toDate,
        regionCode, mkb10Code);
    List<CaseCountDynamicsData> rows = regionalDiseaseDashboardRepository.filterDiseaseAggregatingData(
        diseaseFilterRequest);

    MonitoringDynamicsResponse response = new MonitoringDynamicsResponse();
    response.setToDate(toDate);
    response.setData(rows.stream().map(r -> {
      MonitoringDynamicsResponse.AgeGroupCount ageGroups = new MonitoringDynamicsResponse.AgeGroupCount();
      ageGroups.setAge_0_3(r.age_0_3());
      ageGroups.setAge_3_6(r.age_3_6());
      ageGroups.setAge_6_10(r.age_6_10());
      ageGroups.setAge_10_17(r.age_10_17());
      ageGroups.setAge_18_25(r.age_18_25());
      ageGroups.setAge_25_45(r.age_25_45());
      ageGroups.setAge_45_65(r.age_45_65());
      ageGroups.setAge_65_plus(r.age_65_plus());

      MonitoringDynamicsResponse.Item item = new MonitoringDynamicsResponse.Item();
      item.setPeriod(r.period());
      item.setLocationCode(r.locationCode());
      item.setTotalCases(r.confirmed() + r.unconfirmed());
      item.setConfirmed(r.confirmed());
      item.setUnconfirmed(r.unconfirmed());
      item.setMale(r.male());
      item.setFemale(r.female());
      item.setAgeGroups(ageGroups);
      return item;
    }).toList());
    return response;
  }
}
