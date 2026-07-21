package uz.uzinfocom.app.integration.inbound.oauth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.integration.inbound.oauth.application.exception.InvalidIntegrationCredentialsException;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenRequest;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenResponse;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.jwt.integration.IntegrationTokenIssuer;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegrationTokenService {

    private final IntegrationClientRepository integrationClientRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final IntegrationTokenIssuer tokenIssuer;

    @Transactional
    public IntegrationTokenResponse issueToken(IntegrationTokenRequest request) {
        IntegrationClient client = integrationClientRepository.findByClientId(request.clientId())
                .filter(IntegrationClient::isActive)
                .orElseThrow(InvalidIntegrationCredentialsException::new);

        if (!passwordEncoder.matches(request.clientSecret(), client.getClientSecretHash())) {
            throw new InvalidIntegrationCredentialsException();
        }

        Set<String> scopes = Set.of(client.getScopes().split(","));

        UUID organizationUuid = organizationRepository.findById(client.getOrganizationId())
                .orElseThrow(InvalidIntegrationCredentialsException::new)
                .getUuid();

        IntegrationTokenIssuer.IssuedToken issuedToken = tokenIssuer.issue(
                client.getClientId(), client.getSourceKey(), client.getOrganizationId(), organizationUuid, scopes);

        client.setLastUsedAt(Instant.now());
        integrationClientRepository.save(client);

        return new IntegrationTokenResponse(
                issuedToken.tokenValue(),
                "Bearer",
                issuedToken.expiresInSeconds(),
                String.join(" ", scopes)
        );
    }
}
