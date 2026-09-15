package uz.uzinfocom.app.platform.integrationclient.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.platform.integrationclient.application.OrganizationLookup;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientFilterRequest;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientResponse;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientSourceKeyLookupRequest;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientTableResponse;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientWebhookResponse;
import uz.uzinfocom.app.platform.integrationclient.application.query.specification.IntegrationClientSpecification;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationClientQueryService {

    private final IntegrationClientRepository integrationClientRepository;
    private final OrganizationLookup organizationLookup;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<IntegrationClientTableResponse> findAll(IntegrationClientFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, IntegrationClientSortFields.ALLOWED);

        Page<IntegrationClient> page = integrationClientRepository.findAll(
                IntegrationClientSpecification.byFilter(request),
                pageable
        );

        return page.map(this::toTableResponse);
    }

    @Transactional(readOnly = true)
    public List<String> listActiveSourceKeys(IntegrationClientSourceKeyLookupRequest request) {
        Pageable pageable = PageRequest.of(0, request.normalizedLimit());
        return integrationClientRepository.findDistinctActiveSourceKeys(request.normalizedSearch(), pageable);
    }

    @Transactional(readOnly = true)
    public IntegrationClientResponse getById(Long id) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        return toResponse(client);
    }

    private IntegrationClientTableResponse toTableResponse(IntegrationClient client) {
        return new IntegrationClientTableResponse(
                client.getId(),
                client.getClientId(),
                client.getName(),
                client.getAuthType(),
                client.getSourceKey(),
                client.getOrganizationId(),
                organizationLookup.activeOrganizationNameById(client.getOrganizationId()),
                client.isActive(),
                client.getLastUsedAt()
        );
    }

    private IntegrationClientResponse toResponse(IntegrationClient client) {
        return new IntegrationClientResponse(
                client.getId(),
                client.getClientId(),
                client.getName(),
                client.getAuthType(),
                client.getSourceKey(),
                client.getOrganizationId(),
                organizationLookup.activeOrganizationNameById(client.getOrganizationId()),
                List.of(client.getScopes().split(",")),
                client.isActive(),
                StringUtils.hasText(client.getAllowedIps()) ? List.of(client.getAllowedIps().split(",")) : List.of(),
                client.getLastUsedAt(),
                toWebhookResponse(client),
                auditResolver.resolve(client)
        );
    }

    private IntegrationClientWebhookResponse toWebhookResponse(IntegrationClient client) {
        return new IntegrationClientWebhookResponse(
                client.getWebhookCallbackUrl(),
                client.getWebhookHttpMethod(),
                client.getWebhookAuthType(),
                client.getWebhookAuthUsername(),
                client.getWebhookAuthHeaderName(),
                client.isWebhookActive(),
                StringUtils.hasText(client.getWebhookAuthSecretEncrypted())
        );
    }
}
