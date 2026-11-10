package uz.uzinfocom.app.integration.dhp.employment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.dhp.common.support.DhpNiValidator;
import uz.uzinfocom.app.integration.dhp.employment.application.mapper.DhpEmploymentMapper;
import uz.uzinfocom.app.integration.dhp.employment.client.DhpEmploymentClient;
import uz.uzinfocom.app.integration.dhp.employment.web.dto.EmploymentItem;
import uz.uzinfocom.app.integration.dhp.employment.web.dto.EmploymentResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DhpEmploymentLookupService {

    private final DhpEmploymentClient client;

    public EmploymentResponse lookupByNi(String rawNi) {
        String ni = DhpNiValidator.validate(rawNi);

        List<EmploymentItem> items = client.findByNi(ni)
                .map(payload -> DhpEmploymentMapper.map(payload, LocalDate.now()))
                .orElse(List.of());

        return new EmploymentResponse(ni, items);
    }
}
