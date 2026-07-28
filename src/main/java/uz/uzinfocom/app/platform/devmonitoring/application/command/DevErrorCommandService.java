package uz.uzinfocom.app.platform.devmonitoring.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.command.dto.DevErrorStatusUpdateRequest;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorStatus;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevErrorLogRepository;
import uz.uzinfocom.app.platform.devmonitoring.security.DevUserPrincipal;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.Instant;

/**
 * {@link DevErrorLog} is deliberately not an {@code AuditableEntity} (see its
 * javadoc), so {@code resolvedBy} is set explicitly here from the
 * authenticated {@link DevUserPrincipal} rather than relying on
 * {@code SecurityAuditorAware}, which only recognizes SSO/DHP-authenticated
 * {@code FederatedAuthenticationToken}s.
 */
@Service
@RequiredArgsConstructor
public class DevErrorCommandService {

    private final DevErrorLogRepository devErrorLogRepository;

    @Transactional
    public void updateStatus(Long id, DevErrorStatusUpdateRequest request) {
        DevErrorLog entry = devErrorLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-monitoring.error.not-found", id));

        entry.setStatus(request.status());
        if (request.status() == DevErrorStatus.RESOLVED) {
            entry.setResolvedBy(currentDevUserId());
            entry.setResolvedAt(Instant.now());
        } else {
            entry.setResolvedBy(null);
            entry.setResolvedAt(null);
        }

        devErrorLogRepository.save(entry);
    }

    private Long currentDevUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof DevUserPrincipal principal
                ? principal.getDevUserId()
                : null;
    }
}
