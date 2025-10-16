package uz.uzinfocom.isemid.features.analytic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.analytic.repository.AnalyticTemplateRepository;
import uz.uzinfocom.isemid.features.analytic.service.AnalyticTemplateService;

@Service
@RequiredArgsConstructor
public class AnalyticTemplateServiceImpl implements AnalyticTemplateService {
    private final AnalyticTemplateRepository repository;
}
