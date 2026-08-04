package uz.uzinfocom.app.platform.integrationclient.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationIdResolver;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientAllowedIpsUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.exception.AllowedIpsRequiredException;
import uz.uzinfocom.app.platform.integrationclient.application.exception.InvalidAllowedIpException;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.ip.IpAllowlistMatcher;
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
        String scopes = request.scopes().stream()
                .map(IntegrationScope::getClaim)
                .collect(Collectors.joining(","));

        String allowedIps = normalizeAllowedIps(request.allowedIps());
        if (request.authType() == IntegrationAuthType.IP_ALLOWLIST && allowedIps == null) {
            throw new AllowedIpsRequiredException();
        }

        IssuedCredential credential = issueCredential(clientId, request.authType());

        IntegrationClient client = IntegrationClient.builder()
                .clientId(clientId)
                .authType(request.authType())
                .clientSecretHash(credential.clientSecretHash())
                .apiKeyHash(credential.apiKeyHash())
                .basicAuthSecretHash(credential.basicAuthSecretHash())
                .organizationId(organizationId)
                .sourceKey(sourceKey)
                .name(request.name().trim())
                .scopes(scopes)
                .allowedIps(allowedIps)
                .active(true)
                .build();

        IntegrationClient saved = integrationClientRepository.save(client);
        log.info("Integration client registered. id={}, clientId={}, authType={}, sourceKey={}, organizationId={}",
                saved.getId(), saved.getClientId(), saved.getAuthType(), saved.getSourceKey(), saved.getOrganizationId());

        return new IntegrationClientCreateResponse(
                saved.getId(),
                saved.getClientId(),
                saved.getAuthType(),
                credential.clientSecret(),
                credential.apiKey(),
                credential.basicAuthSecret(),
                saved.getName(),
                saved.getSourceKey(),
                saved.getOrganizationId(),
                List.of(saved.getScopes().split(",")),
                toAllowedIpsList(saved.getAllowedIps())
        );
    }

    /**
     * Generates and hashes the one-time secret material for the given
     * {@link IntegrationAuthType}, or none at all for {@code IP_ALLOWLIST},
     * which is identified purely by {@code allowedIps}.
     */
    private IssuedCredential issueCredential(String clientId, IntegrationAuthType authType) {
        return switch (authType) {
            case CLIENT_CREDENTIALS -> {
                String clientSecret = generateRandomToken();
                yield new IssuedCredential(passwordEncoder.encode(clientSecret), null, null, clientSecret, null, null);
            }
            case API_KEY -> {
                String apiKeyToken = generateRandomToken();
                String apiKey = clientId + "." + apiKeyToken;
                yield new IssuedCredential(null, passwordEncoder.encode(apiKeyToken), null, null, apiKey, null);
            }
            case BASIC_AUTH -> {
                String basicAuthSecret = generateRandomToken();
                yield new IssuedCredential(null, null, passwordEncoder.encode(basicAuthSecret), null, null, basicAuthSecret);
            }
            case IP_ALLOWLIST -> new IssuedCredential(null, null, null, null, null, null);
        };
    }

    /** Hashes to persist plus plaintext values to return once, paired by {@link IntegrationAuthType}. */
    private record IssuedCredential(
            String clientSecretHash,
            String apiKeyHash,
            String basicAuthSecretHash,
            String clientSecret,
            String apiKey,
            String basicAuthSecret
    ) {
    }

    /**
     * Updates the caller-editable fields only - name, scopes, and the active
     * flag. {@code clientId}, {@code authType}, {@code sourceKey},
     * {@code organizationId} and credentials stay fixed for the client's
     * lifetime; rotating any of those means revoking this client and
     * registering a new one. Unlike {@link #revoke}, {@code active} is
     * reversible here - it can be flipped back to {@code true}.
     */
    @Transactional
    public void update(Long id, IntegrationClientUpdateRequest request) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        String scopes = request.scopes().stream()
                .map(IntegrationScope::getClaim)
                .collect(Collectors.joining(","));

        client.setName(request.name().trim());
        client.setScopes(scopes);
        client.setActive(request.active());
        integrationClientRepository.save(client);

        log.info("Integration client updated. id={}, clientId={}, active={}",
                client.getId(), client.getClientId(), client.isActive());
    }

    @Transactional
    public void revoke(Long id) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        client.setActive(false);
        integrationClientRepository.save(client);

        log.info("Integration client revoked. id={}, clientId={}", client.getId(), client.getClientId());
    }

    @Transactional
    public void updateAllowedIps(Long id, IntegrationClientAllowedIpsUpdateRequest request) {
        IntegrationClient client = integrationClientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("integration-client.not-found", id));

        String allowedIps = normalizeAllowedIps(request.allowedIps());
        if (client.getAuthType() == IntegrationAuthType.IP_ALLOWLIST && allowedIps == null) {
            // allowedIps is the only thing identifying this client - clearing it would
            // leave it with no way to ever authenticate again.
            throw new AllowedIpsRequiredException();
        }

        client.setAllowedIps(allowedIps);
        integrationClientRepository.save(client);

        log.info("Integration client allow-list updated. id={}, clientId={}", client.getId(), client.getClientId());
    }

    private static String normalizeAllowedIps(List<String> allowedIps) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return null;
        }

        List<String> trimmed = allowedIps.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();

        for (String entry : trimmed) {
            if (!IpAllowlistMatcher.isValidEntry(entry)) {
                throw new InvalidAllowedIpException(entry);
            }
        }

        return trimmed.isEmpty() ? null : String.join(",", trimmed);
    }

    private static List<String> toAllowedIpsList(String allowedIps) {
        return StringUtils.hasText(allowedIps) ? List.of(allowedIps.split(",")) : List.of();
    }

    private static String generateRandomToken() {
        byte[] randomBytes = new byte[CLIENT_SECRET_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return URL_ENCODER.encodeToString(randomBytes);
    }
}
