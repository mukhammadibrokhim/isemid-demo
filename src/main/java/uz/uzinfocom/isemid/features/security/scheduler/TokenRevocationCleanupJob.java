package uz.uzinfocom.isemid.features.security.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.features.security.repository.TokenRevocationRepository;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenRevocationCleanupJob {

    private final TokenRevocationRepository repository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        repository.deleteExpired(Instant.now());
    }
}
