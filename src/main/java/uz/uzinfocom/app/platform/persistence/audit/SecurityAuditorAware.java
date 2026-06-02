package uz.uzinfocom.app.platform.persistence.audit;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Optional;

@Component("securityAuditorAware")
public class SecurityAuditorAware implements AuditorAware<Long>, CurrentAuditProvider {

    @PostConstruct
    void registerAuditProvider() {
        CurrentAuditContext.setProvider(this);
    }

    @NonNull
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(currentUserId());
    }

    @Override
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof FederatedAuthenticationToken token)) {
            return null;
        }

        return token.getUserId();
    }

    @Override
    public Organization currentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElse(null);
    }
}