package uz.uzinfocom.isemid.features.analytic.service;

import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticTemplateRequest;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticTemplate;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;

public interface AnalyticTemplateService {
    List<AnalyticTemplate> getAll(User user);

    AnalyticTemplate create(AnalyticTemplateRequest request, User user);

    AnalyticTemplate update(Long id, AnalyticTemplate updated, User user);

    void delete(Long id, User user);
}
