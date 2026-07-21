package uz.uzinfocom.app.platform.integrationclient.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationIdResolver;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationClientCommandService {

    private static final int CLIENT_SECRET_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final IntegrationClientRepository integrationClientRepository;
    private final OrganizationIdResolver organizationIdResolver;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public IntegrationClientCreateResponse create(IntegrationClientCreateRequest request) {
        Long organizationId = organizationIdResolver.resolveActiveId(request.organizationId());

        String sourceKey = request.sourceKey().trim().toLowerCase(Locale.ROOT);
        if (integrationClientRepository.existsBySourceKey(sourceKey)) {
            throw new ConflictException("integration-client.source-key.already-exists", sourceKey);
        }

        String clientId = "ic_" + generateRandomToken();
        String clientSecret = generateRandomToken();
        String scopes = request.scopes().stream()
                .map(IntegrationScope::getClaim)
                .collect(Collectors.joining(","));

        IntegrationClient client = IntegrationClient.builder()
                .clientId(clientId)
                .clientSecretHash(passwordEncoder.encode(clientSecret))
                .organizationId(organizationId)
                .sourceKey(sourceKey)
                .name(request.name().trim())
                .scopes(scopes)
                .active(true)
                .build();

        IntegrationClient saved = integrationClientRepository.save(client);
        log.info("Integration client registered. id={}, clientId={}, sourceKey={}, organizationId={}",
                saved.getId(), saved.getClientId(), saved.getSourceKey(), saved.getOrganizationId());

        return new IntegrationClientCreateResponse(
                saved.getId(),
                saved.getClientId(),
                clientSecret,
                saved.getName(),
                saved.getSourceKey(),
                saved.getOrganizationId(),
                List.of(saved.getScopes().split(","))
        );
    }

    @Transactional
    public void revoke(Long id) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        client.setActive(false);
        integrationClientRepository.save(client);

        log.info("Integration client revoked. id={}, clientId={}", client.getId(), client.getClientId());
    }

    private static String generateRandomToken() {
        byte[] randomBytes = new byte[CLIENT_SECRET_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return URL_ENCODER.encodeToString(randomBytes);
    }
}
