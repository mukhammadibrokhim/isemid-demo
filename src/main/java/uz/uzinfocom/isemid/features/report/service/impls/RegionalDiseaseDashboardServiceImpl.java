package uz.uzinfocom.isemid.features.report.service.impls;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.service.RegionalDiseaseDashboardService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionalDiseaseDashboardServiceImpl implements RegionalDiseaseDashboardService {

  @Override
  public MonitoringResponse getMonitoring(LocalDate fromDate, LocalDate toDate, String regionCode,
      String mkb10Code) {
    return null;
  }
}
