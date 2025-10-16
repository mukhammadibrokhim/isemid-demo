package uz.uzinfocom.isemid.features.analytic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticTemplateRequest;
import uz.uzinfocom.isemid.features.analytic.mapper.AnalyticTemplateMapper;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticTemplate;
import uz.uzinfocom.isemid.features.analytic.repository.AnalyticTemplateRepository;
import uz.uzinfocom.isemid.features.analytic.service.AnalyticTemplateService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticTemplateServiceImpl implements AnalyticTemplateService {
    private final AnalyticTemplateRepository repository;
    private final AnalyticTemplateMapper mapper;

    @Override
    public List<AnalyticTemplate> getAll(User user) {
        return repository.findAllByOwnerIdOrGlobalTrue(user.getId());
    }

    @Override
    public AnalyticTemplate create(AnalyticTemplateRequest request, User user) {
        AnalyticTemplate template = mapper.toEntity(request);
        template.setOwner(user);
        return repository.save(template);
    }

    @Override
    public AnalyticTemplate update(Long id, AnalyticTemplate updated, User user) {
        AnalyticTemplate existing = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Template not found"));
        if (!existing.getOwner().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot edit this template");
        }
        existing.setName(updated.getName());
        existing.setContent(updated.getContent());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id, User user) {
        AnalyticTemplate template = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Template not found"));
        if (!template.getOwner().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot delete this template");
        }
        repository.delete(template);
    }
}
