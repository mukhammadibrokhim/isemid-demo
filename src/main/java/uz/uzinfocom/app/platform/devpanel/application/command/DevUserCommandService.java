package uz.uzinfocom.app.platform.devpanel.application.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserChangePasswordRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserCreateRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserCreateResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserResetPasswordResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserUpdateRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.DevPositionQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.DevUserQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserDetailResponse;
import uz.uzinfocom.app.platform.devpanel.domain.DevPosition;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;
import uz.uzinfocom.app.platform.devpanel.security.DevUserPrincipal;
import uz.uzinfocom.app.shared.exception.ConflictException;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class DevUserCommandService {

    private static final int PASSWORD_BYTES = 24;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final DevUserRepository devUserRepository;
    private final DevUserQueryService devUserQueryService;
    private final DevPositionQueryService devPositionQueryService;
    private final PasswordEncoder devUserPasswordEncoder;

    public DevUserCommandService(
            DevUserRepository devUserRepository,
            DevUserQueryService devUserQueryService,
            DevPositionQueryService devPositionQueryService,
            @Qualifier("devUserPasswordEncoder") PasswordEncoder devUserPasswordEncoder
    ) {
        this.devUserRepository = devUserRepository;
        this.devUserQueryService = devUserQueryService;
        this.devPositionQueryService = devPositionQueryService;
        this.devUserPasswordEncoder = devUserPasswordEncoder;
    }

    @Transactional
    public DevUserCreateResponse create(DevUserCreateRequest request) {
        String username = request.username().trim();
        if (devUserRepository.existsByUsername(username)) {
            throw new ConflictException("dev-user.username.already-exists", username);
        }

        DevUserRole role = request.role() != null ? request.role() : DevUserRole.USER;
        if (role == DevUserRole.SUPER_ADMIN) {
            throw new ConflictException("dev-user.role.super-admin-not-creatable");
        }

        String password = generateRandomPassword();

        DevPosition position = request.positionId() != null
                ? devPositionQueryService.findEntity(request.positionId())
                : null;

        DevUser devUser = DevUser.builder()
                .username(username)
                .passwordHash(devUserPasswordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .email(request.email().trim())
                .fullName(request.fullName())
                .phone(request.phone())
                .position(position)
                .build();

        DevUser saved = devUserRepository.save(devUser);
        log.info("Dev-panel account provisioned. id={}, username={}, role={}", saved.getId(), saved.getUsername(), role);

        return new DevUserCreateResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getRole(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getPhone(),
                position != null ? position.getId() : null,
                position != null ? position.getName() : null,
                password,
                saved.getCreatedAt()
        );
    }

    @Transactional
    public DevUserDetailResponse updateProfile(Long id, DevUserUpdateRequest request) {
        DevUser devUser = devUserQueryService.findEntity(id);

        DevPosition position = request.positionId() != null
                ? devPositionQueryService.findEntity(request.positionId())
                : null;

        devUser.setEmail(request.email().trim());
        devUser.setFullName(request.fullName());
        devUser.setPhone(request.phone());
        devUser.setPosition(position);
        devUserRepository.save(devUser);

        return DevUserQueryService.toDetailResponse(devUser);
    }

    /**
     * Self-service only - {@code id} always comes from the caller's own
     * {@link DevUserPrincipal}, never a path/body-supplied value, so an
     * account can only ever change its own password here.
     */
    @Transactional
    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION, allEntries = true)
    public void changeOwnPassword(Long id, DevUserChangePasswordRequest request) {
        DevUser devUser = devUserQueryService.findEntity(id);

        if (!devUserPasswordEncoder.matches(request.currentPassword(), devUser.getPasswordHash())) {
            throw new ConflictException("dev-user.password.current.invalid");
        }

        devUser.setPasswordHash(devUserPasswordEncoder.encode(request.newPassword()));
        devUser.setMustChangePassword(false);
        devUserRepository.save(devUser);

        log.info("Dev-panel account changed its own password. id={}, username={}", devUser.getId(), devUser.getUsername());
    }

    @Transactional
    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION, allEntries = true)
    public void revoke(Long id) {
        if (id.equals(currentDevUserId())) {
            throw new ConflictException("dev-user.revoke.self-forbidden");
        }

        DevUser devUser = devUserQueryService.findEntity(id);

        devUser.setEnabled(false);
        devUserRepository.save(devUser);

        log.info("Dev-panel account revoked. id={}, username={}", devUser.getId(), devUser.getUsername());
    }

    /**
     * The counterpart to {@link #revoke(Long)} - re-enables a previously
     * revoked account. Idempotent: a no-op if the account is already enabled.
     */
    @Transactional
    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION, allEntries = true)
    public void unblock(Long id) {
        DevUser devUser = devUserQueryService.findEntity(id);

        devUser.setEnabled(true);
        devUserRepository.save(devUser);

        log.info("Dev-panel account unblocked. id={}, username={}", devUser.getId(), devUser.getUsername());
    }

    /**
     * Admin-initiated reset, distinct from {@link #changeOwnPassword} - no
     * current-password check, since a SUPER_ADMIN issuing this already has
     * full control over the account. Sets {@code mustChangePassword} so the
     * account is forced to set its own password on next login, same as a
     * freshly-{@link #create}d one.
     */
    @Transactional
    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_AUTHENTICATION, allEntries = true)
    public DevUserResetPasswordResponse resetPassword(Long id) {
        DevUser devUser = devUserQueryService.findEntity(id);

        String password = generateRandomPassword();
        devUser.setPasswordHash(devUserPasswordEncoder.encode(password));
        devUser.setMustChangePassword(true);
        devUserRepository.save(devUser);

        log.info("Dev-panel account password reset by an admin. id={}, username={}", devUser.getId(), devUser.getUsername());

        return new DevUserResetPasswordResponse(devUser.getId(), devUser.getUsername(), password);
    }

    private static Long currentDevUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof DevUserPrincipal principal
                ? principal.getDevUserId()
                : null;
    }

    private static String generateRandomPassword() {
        byte[] randomBytes = new byte[PASSWORD_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return URL_ENCODER.encodeToString(randomBytes);
    }
}
