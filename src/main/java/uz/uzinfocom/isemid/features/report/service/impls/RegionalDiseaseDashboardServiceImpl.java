package uz.uzinfocom.isemid.features.report.service.impls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.report.dtos.CaseCountDynamicsData;
import uz.uzinfocom.isemid.features.report.dtos.request.DiseaseFilterRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.MonitoringDynamicsResponse;
import uz.uzinfocom.isemid.features.report.repository.RegionalDiseaseDashboardRepository;
import uz.uzinfocom.isemid.features.report.service.RegionalDiseaseDashboardService;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionalDiseaseDashboardServiceImpl implements RegionalDiseaseDashboardService {

  private final RegionalDiseaseDashboardRepository regionalDiseaseDashboardRepository;
  private final Mkb10Repository mkb10Repository;

  @Override
  public MonitoringDynamicsResponse getMonitoring(LocalDate fromDate, LocalDate toDate,
      String regionCode, String mkb10Code) {
    List<CaseCountDynamicsData> rows = regionalDiseaseDashboardRepository.filterDiseaseAggregatingData(
        new DiseaseFilterRequest(fromDate, toDate, regionCode, mkb10Code));

    Map<String, MonitoringDynamicsResponse.Item> itemByKey = new LinkedHashMap<>();
    Map<String, String> mkbToBlockCache = new HashMap<>();

    for (CaseCountDynamicsData r : rows) {
      MonitoringDynamicsResponse.Item item = itemByKey.computeIfAbsent(
          r.period() + "|" + r.locationCode(), k -> newItem(r.period(), r.locationCode()));
      accumulate(item, r, mkbToBlockCache);
    }

    MonitoringDynamicsResponse response = new MonitoringDynamicsResponse();
    response.setToDate(toDate);
    response.setReport(new ArrayList<>(itemByKey.values()));
    return response;
  }

  private MonitoringDynamicsResponse.Item newItem(String period, String locationCode) {
    MonitoringDynamicsResponse.Item item = new MonitoringDynamicsResponse.Item();
    item.setPeriod(period);
    item.setLocationCode(locationCode);
    return item;
  }

  private void accumulate(MonitoringDynamicsResponse.Item item, CaseCountDynamicsData r,
      Map<String, String> cache) {
    item.setTotalCases(item.getTotalCases() + r.confirmed() + r.unconfirmed());
    item.setConfirmed(item.getConfirmed() + r.confirmed());
    item.setUnconfirmed(item.getUnconfirmed() + r.unconfirmed());
    item.setMale(item.getMale() + r.male());
    item.setFemale(item.getFemale() + r.female());

    MonitoringDynamicsResponse.AgeGroupCount ag = item.getAgeGroups();
    ag.setAge_0_3(ag.getAge_0_3() + r.age_0_3());
    ag.setAge_3_6(ag.getAge_3_6() + r.age_3_6());
    ag.setAge_6_10(ag.getAge_6_10() + r.age_6_10());
    ag.setAge_10_17(ag.getAge_10_17() + r.age_10_17());
    ag.setAge_18_25(ag.getAge_18_25() + r.age_18_25());
    ag.setAge_25_45(ag.getAge_25_45() + r.age_25_45());
    ag.setAge_45_65(ag.getAge_45_65() + r.age_45_65());
    ag.setAge_65_plus(ag.getAge_65_plus() + r.age_65_plus());

    if (hasText(r.finalMkb10Code())) {
      String blockCode = cache.computeIfAbsent(r.finalMkb10Code().trim(), this::resolveBlockCodeOrSelf);
      if (!"-".equals(blockCode)) {
        item.getDiseaseType().merge(blockCode, r.confirmed() + r.unconfirmed(), Integer::sum);
      }
    }
  }

  private String resolveBlockCodeOrSelf(String mkbCode) {
    String code = mkbCode.trim();
    if (code.isEmpty() || "string".equalsIgnoreCase(code)) return "-";

    Optional<Mkb10> opt = mkb10Repository.findByCode(code);
    if (opt.isEmpty()) return formatMkbCode(code);

    Mkb10 current = opt.get();
    for (int i = 0; i < 20; i++) {
      String currentCode = current.getCode();
      if (hasText(currentCode) && currentCode.trim().contains("-")) {
        return formatMkbCode(currentCode.trim());
      }
      if (current.getParentId() == null) break;

      Optional<Mkb10> parent = mkb10Repository.findById(current.getParentId());
      if (parent.isEmpty()) break;

      current = parent.get();
    }

    return formatMkbCode(code);
  }

  private String formatMkbCode(String code) {
    if (!hasText(code) || "string".equalsIgnoreCase(code.trim())) return "-";
    String trimmed = code.trim();
    return (trimmed.startsWith("(") && trimmed.endsWith(")")) ? trimmed : "(" + trimmed + ")";
  }
}
