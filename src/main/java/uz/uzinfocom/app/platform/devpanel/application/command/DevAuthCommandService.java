package uz.uzinfocom.app.platform.devpanel.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLoginRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLoginResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevOtpVerifyRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTicketResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTwoFactorConfirmResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTwoFactorSetupResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.DevUserQueryService;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;
import uz.uzinfocom.app.platform.devpanel.security.DevAuthProperties;
import uz.uzinfocom.app.platform.devpanel.security.DevLoginAttemptLimiter;
import uz.uzinfocom.app.platform.devpanel.security.DevTokenService;
import uz.uzinfocom.app.platform.devpanel.security.DevTotpSecretCipher;
import uz.uzinfocom.app.platform.devpanel.security.DevUserPrincipal;
import uz.uzinfocom.app.platform.devpanel.security.Totp;
import uz.uzinfocom.app.shared.exception.ConflictException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Login and two-factor lifecycle of the dev panel.
 *
 * <p>Login order matters: the {@link DevLoginAttemptLimiter} is checked first,
 * a wrong password OR a wrong second factor both count as one failure, and the
 * counter is only cleared after the <em>whole</em> login succeeded - clearing it
 * after the password alone would let someone who knows the password brute-force
 * the 6-digit code. Every failure answers the same generic 401.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DevAuthCommandService {

    private static final int TOTP_WINDOW_STEPS = 1;
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final int RECOVERY_CODE_CHARS = 10;
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DaoAuthenticationProvider devUserAuthenticationProvider;
    private final DevLoginAttemptLimiter loginAttemptLimiter;
    private final DevUserRepository devUserRepository;
    private final DevUserQueryService devUserQueryService;
    private final DevTokenService tokenService;
    private final DevTotpSecretCipher totpSecretCipher;
    private final DevAuthProperties authProperties;

    public DevLoginResponse login(DevLoginRequest request, String ip) {
        String username = request.username().trim();

        if (loginAttemptLimiter.isBlocked(username, ip)) {
            log.warn("Dev-panel login refused: too many failed attempts. username={}, ip={}", username, ip);
            throw new BadCredentialsException("Bad credentials");
        }

        DevUserPrincipal authenticated;
        try {
            authenticated = (DevUserPrincipal) devUserAuthenticationProvider
                    .authenticate(new UsernamePasswordAuthenticationToken(username, request.password()))
                    .getPrincipal();
        } catch (AuthenticationException failure) {
            loginAttemptLimiter.recordFailure(username, ip);
            throw new BadCredentialsException("Bad credentials");
        }

        // Re-read: the authenticated principal could be stale about 2FA state.
        DevUser devUser = devUserRepository.findById(authenticated.getDevUserId())
                .filter(DevUser::isEnabled)
                .orElseThrow(() -> {
                    loginAttemptLimiter.recordFailure(username, ip);
                    return new BadCredentialsException("Bad credentials");
                });

        boolean mfaVerified;
        if (devUser.isTotpEnabled()) {
            if (request.otp() == null || request.otp().isBlank()) {
                // Two-step login, step 1: password is right, now ask for the authenticator code.
                // Deliberately NOT clearing the failure counter here - only a completed login does,
                // otherwise someone who knows the password could reset it between code guesses.
                DevTokenService.IssuedToken challenge = tokenService.issueChallenge(new DevUserPrincipal(devUser));
                log.info("Dev-panel login: OTP challenge issued. id={}, username={}, ip={}",
                        devUser.getId(), devUser.getUsername(), ip);
                return DevLoginResponse.otpChallenge(
                        challenge.token(), challenge.expiresAt(), devUser.isMustChangePassword());
            }
            if (!verifySecondFactor(devUser, request.otp())) {
                loginAttemptLimiter.recordFailure(username, ip);
                log.warn("Dev-panel login refused: bad second factor. username={}, ip={}", username, ip);
                throw new BadCredentialsException("Bad credentials");
            }
            mfaVerified = true;
        } else {
            // Not enrolled yet: a full session only if the deployment does not demand 2FA.
            mfaVerified = !authProperties.twoFactorRequired();
        }

        loginAttemptLimiter.recordSuccess(username);
        log.info("Dev-panel login. id={}, username={}, mfaVerified={}, ip={}",
                devUser.getId(), devUser.getUsername(), mfaVerified, ip);
        return session(devUser, mfaVerified);
    }

    /**
     * Two-step login, step 2: redeems the challenge from {@link #login} with the
     * authenticator (or recovery) code. A wrong/expired challenge, a changed
     * password/2FA state in between, a locked account and a wrong code all give
     * the same generic 401 and count toward the same lockout.
     */
    public DevLoginResponse verifyOtp(DevOtpVerifyRequest request, String ip) {
        DevTokenService.TokenClaims claims = tokenService.parseChallenge(request.challengeToken())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        DevUser devUser = devUserRepository.findById(claims.devUserId())
                .filter(DevUser::isEnabled)
                .filter(DevUser::isTotpEnabled)
                .filter(user -> new DevUserPrincipal(user).getCredentialVersion().equals(claims.credentialVersion()))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        String username = devUser.getUsername();
        if (loginAttemptLimiter.isBlocked(username, ip)) {
            log.warn("Dev-panel OTP step refused: too many failed attempts. username={}, ip={}", username, ip);
            throw new BadCredentialsException("Bad credentials");
        }
        if (!verifySecondFactor(devUser, request.otp())) {
            loginAttemptLimiter.recordFailure(username, ip);
            log.warn("Dev-panel OTP step refused: bad code. username={}, ip={}", username, ip);
            throw new BadCredentialsException("Bad credentials");
        }

        loginAttemptLimiter.recordSuccess(username);
        log.info("Dev-panel login. id={}, username={}, mfaVerified=true, ip={}", devUser.getId(), username, ip);
        return session(devUser, true);
    }

    public DevTicketResponse issueTicket(Long devUserId) {
        return new DevTicketResponse(tokenService.issueTicket(devUserId), DevTokenService.TICKET_TTL_SECONDS);
    }

    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_PRINCIPAL, allEntries = true)
    public DevTwoFactorSetupResponse startTwoFactorSetup(Long devUserId) {
        DevUser devUser = devUserQueryService.findEntity(devUserId);
        if (devUser.isTotpEnabled()) {
            throw new ConflictException("dev-user.two-factor.already-enabled");
        }

        String secret = Totp.generateSecret();
        devUser.setTotpSecret(totpSecretCipher.encrypt(secret));
        devUserRepository.save(devUser);

        log.info("Dev-panel two-factor setup started. id={}, username={}", devUser.getId(), devUser.getUsername());
        return new DevTwoFactorSetupResponse(
                secret,
                Totp.otpauthUri(authProperties.issuer(), devUser.getUsername(), secret)
        );
    }

    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_PRINCIPAL, allEntries = true)
    public DevTwoFactorConfirmResponse confirmTwoFactor(Long devUserId, String code, String ip) {
        DevUser devUser = devUserQueryService.findEntity(devUserId);
        if (devUser.isTotpEnabled()) {
            throw new ConflictException("dev-user.two-factor.already-enabled");
        }
        if (devUser.getTotpSecret() == null) {
            throw new ConflictException("dev-user.two-factor.setup-not-started");
        }

        String username = devUser.getUsername();
        if (loginAttemptLimiter.isBlocked(username, ip)) {
            throw new ConflictException("dev-user.two-factor.invalid-code");
        }

        long step = Totp.matchStep(
                totpSecretCipher.decrypt(devUser.getTotpSecret()), code,
                Instant.now().getEpochSecond(), TOTP_WINDOW_STEPS);
        if (step < 0) {
            loginAttemptLimiter.recordFailure(username, ip);
            throw new ConflictException("dev-user.two-factor.invalid-code");
        }

        List<String> recoveryCodes = generateRecoveryCodes();
        devUser.setTotpEnabled(true);
        devUser.setTotpLastUsedStep(step);
        devUser.setRecoveryCodeHashes(String.join("\n", recoveryCodes.stream().map(DevAuthCommandService::hash).toList()));
        DevUser saved = devUserRepository.save(devUser);

        log.info("Dev-panel two-factor enabled. id={}, username={}", saved.getId(), saved.getUsername());
        return new DevTwoFactorConfirmResponse(session(saved, true), recoveryCodes);
    }

    /** SUPER_ADMIN only (see controller): clears the factor so the account can re-enrol. */
    @CacheEvict(cacheManager = "securityCacheManager", cacheNames = SecurityCacheNames.DEV_PANEL_PRINCIPAL, allEntries = true)
    public void resetTwoFactor(Long devUserId) {
        DevUser devUser = devUserQueryService.findEntity(devUserId);
        devUser.setTotpSecret(null);
        devUser.setTotpEnabled(false);
        devUser.setTotpLastUsedStep(null);
        devUser.setRecoveryCodeHashes(null);
        devUserRepository.save(devUser);

        log.info("Dev-panel two-factor reset by an admin. id={}, username={}", devUser.getId(), devUser.getUsername());
    }

    private DevLoginResponse session(DevUser devUser, boolean mfaVerified) {
        DevTokenService.IssuedToken issued = tokenService.issue(new DevUserPrincipal(devUser), mfaVerified);
        return DevLoginResponse.session(
                issued.token(),
                issued.expiresAt(),
                !mfaVerified,
                devUser.isMustChangePassword()
        );
    }

    /**
     * Accepts a current TOTP code (each time step usable once) or, failing
     * that, an unused recovery code (consumed on use). Persists whichever
     * state changed.
     */
    private boolean verifySecondFactor(DevUser devUser, String otp) {
        if (otp == null || otp.isBlank()) {
            return false;
        }
        String candidate = otp.trim();

        if (candidate.matches("\\d{6}")) {
            long step = Totp.matchStep(
                    totpSecretCipher.decrypt(devUser.getTotpSecret()), candidate,
                    Instant.now().getEpochSecond(), TOTP_WINDOW_STEPS);
            Long lastUsed = devUser.getTotpLastUsedStep();
            if (step >= 0 && (lastUsed == null || step > lastUsed)) {
                devUser.setTotpLastUsedStep(step);
                devUserRepository.save(devUser);
                return true;
            }
            return false;
        }

        return consumeRecoveryCode(devUser, candidate);
    }

    private boolean consumeRecoveryCode(DevUser devUser, String candidate) {
        if (devUser.getRecoveryCodeHashes() == null || devUser.getRecoveryCodeHashes().isBlank()) {
            return false;
        }
        String presented = hash(normalizeRecoveryCode(candidate));
        List<String> remaining = new ArrayList<>(Arrays.asList(devUser.getRecoveryCodeHashes().split("\n")));

        String matched = null;
        for (String stored : remaining) {
            // No early exit: constant work whatever the input.
            if (MessageDigest.isEqual(
                    stored.getBytes(StandardCharsets.UTF_8), presented.getBytes(StandardCharsets.UTF_8))) {
                matched = stored;
            }
        }
        if (matched == null) {
            return false;
        }

        remaining.remove(matched);
        devUser.setRecoveryCodeHashes(remaining.isEmpty() ? null : String.join("\n", remaining));
        devUserRepository.save(devUser);
        log.warn("Dev-panel recovery code used. id={}, username={}, remaining={}",
                devUser.getId(), devUser.getUsername(), remaining.size());
        return true;
    }

    private static List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            StringBuilder code = new StringBuilder(RECOVERY_CODE_CHARS + 1);
            for (int j = 0; j < RECOVERY_CODE_CHARS; j++) {
                if (j == RECOVERY_CODE_CHARS / 2) {
                    code.append('-');
                }
                code.append(RECOVERY_ALPHABET.charAt(SECURE_RANDOM.nextInt(RECOVERY_ALPHABET.length())));
            }
            codes.add(code.toString());
        }
        return codes;
    }

    private static String normalizeRecoveryCode(String code) {
        return code.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }

    /** Hash of the normalized (dash-less, upper-case) form, so entry style does not matter. */
    private static String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizeRecoveryCode(code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is not available in this JVM", unavailable);
        }
    }
}
