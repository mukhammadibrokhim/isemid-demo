package uz.uzinfocom.app.platform.integrationclient.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientResponse;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationClientQueryService {

    private final IntegrationClientRepository integrationClientRepository;
    private final OrganizationMappingHelper organizationMappingHelper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public List<IntegrationClientResponse> findAll() {
        return integrationClientRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IntegrationClientResponse getById(Long id) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        return toResponse(client);
    }

    private IntegrationClientResponse toResponse(IntegrationClient client) {
        return new IntegrationClientResponse(
                client.getId(),
                client.getClientId(),
                client.getName(),
                client.getSourceKey(),
                client.getOrganizationId(),
                organizationMappingHelper.activeOrganizationNameById(client.getOrganizationId()),
                List.of(client.getScopes().split(",")),
                client.isActive(),
                client.getLastUsedAt(),
                auditResolver.resolve(client)
        );
    }
}
