package uz.uzinfocom.app.platform.devmonitoring.application.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.command.dto.DevUserCreateRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.command.dto.DevUserCreateResponse;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevUserRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class DevUserCommandService {

    private static final int PASSWORD_BYTES = 24;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final DevUserRepository devUserRepository;
    private final PasswordEncoder devUserPasswordEncoder;

    public DevUserCommandService(
            DevUserRepository devUserRepository,
            @Qualifier("devUserPasswordEncoder") PasswordEncoder devUserPasswordEncoder
    ) {
        this.devUserRepository = devUserRepository;
        this.devUserPasswordEncoder = devUserPasswordEncoder;
    }

    @Transactional
    public DevUserCreateResponse create(DevUserCreateRequest request) {
        String username = request.username().trim();
        if (devUserRepository.existsByUsername(username)) {
            throw new ConflictException("dev-user.username.already-exists", username);
        }

        String password = generateRandomPassword();

        boolean root = Boolean.TRUE.equals(request.root());

        DevUser devUser = DevUser.builder()
                .username(username)
                .passwordHash(devUserPasswordEncoder.encode(password))
                .enabled(true)
                .root(root)
                .build();

        DevUser saved = devUserRepository.save(devUser);
        log.info("Dev-panel account provisioned. id={}, username={}, root={}", saved.getId(), saved.getUsername(), root);

        return new DevUserCreateResponse(saved.getId(), saved.getUsername(), saved.isRoot(), password, saved.getCreatedAt());
    }

    @Transactional
    public void revoke(Long id) {
        DevUser devUser = devUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-user.not-found", id));

        devUser.setEnabled(false);
        devUserRepository.save(devUser);

        log.info("Dev-panel account revoked. id={}, username={}", devUser.getId(), devUser.getUsername());
    }

    private static String generateRandomPassword() {
        byte[] randomBytes = new byte[PASSWORD_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return URL_ENCODER.encodeToString(randomBytes);
    }
}
