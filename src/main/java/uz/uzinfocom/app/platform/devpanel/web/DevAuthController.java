package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevAuthCommandService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLoginRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLoginResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevOtpVerifyRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTicketResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTwoFactorConfirmRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTwoFactorConfirmResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevTwoFactorSetupResponse;
import uz.uzinfocom.app.platform.devpanel.security.DevUserPrincipal;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "Dev Panel - Auth",
        description = "Login (password + authenticator code) and two-factor enrolment for the developer "
                + "monitoring panel (/v1/dev/**). POST .../auth/login is the only unauthenticated endpoint."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevAuthController {

    private final DevAuthCommandService devAuthCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Log in to the dev panel",
            description = "Step 1: username + password. An account with an authenticator app gets "
                    + "otpRequired=true and a challengeToken (no access yet) - then call POST "
                    + ".../auth/verify-otp with the 6-digit code. An account without 2FA gets its "
                    + "token directly (limited until it enrols). Sending 'otp' here too still works as a "
                    + "one-request login. A wrong password answers 401, same as a locked account."
    )
    @PostMapping(ApiPaths.Dev.AUTH_LOGIN)
    public ApiResponse<DevLoginResponse> login(
            @Valid @RequestBody DevLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                devAuthCommandService.login(request, httpRequest.getRemoteAddr())
        );
    }

    @Operation(
            summary = "Log in, step 2: authenticator code",
            description = "Send the challengeToken from POST .../auth/login (otpRequired=true) together with "
                    + "the 6-digit code from Google Authenticator / Authy (or a one-time recovery code). "
                    + "Returns the same session as a one-step login. Any failure is the same generic 401 "
                    + "and counts toward the lockout."
    )
    @PostMapping(ApiPaths.Dev.AUTH_VERIFY_OTP)
    public ApiResponse<DevLoginResponse> verifyOtp(
            @Valid @RequestBody DevOtpVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                devAuthCommandService.verifyOtp(request, httpRequest.getRemoteAddr())
        );
    }

    @Operation(
            summary = "Get a single-use ticket for an SSE stream",
            description = "EventSource cannot send an Authorization header. Call this with the Bearer token, "
                    + "then open the stream with '?ticket=<ticket>' (valid for a few seconds, one use)."
    )
    @PostMapping(ApiPaths.Dev.AUTH_TICKET)
    public ApiResponse<DevTicketResponse> ticket(@AuthenticationPrincipal DevUserPrincipal principal) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                devAuthCommandService.issueTicket(principal.getDevUserId())
        );
    }

    @Operation(
            summary = "Start authenticator-app enrolment",
            description = "Generates a fresh TOTP secret for the calling account and returns it with an "
                    + "otpauth:// URI (render as a QR code for Google Authenticator / Authy / Microsoft "
                    + "Authenticator). The factor is not active until POST .../2fa/confirm succeeds; "
                    + "calling this again before that replaces the pending secret."
    )
    @PostMapping(ApiPaths.Dev.DEV_USER_ME_2FA_SETUP)
    public ApiResponse<DevTwoFactorSetupResponse> setupTwoFactor(@AuthenticationPrincipal DevUserPrincipal principal) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                devAuthCommandService.startTwoFactorSetup(principal.getDevUserId())
        );
    }

    @Operation(
            summary = "Confirm authenticator-app enrolment",
            description = "Send the first 6-digit code from the app. Turns two-factor on, returns a fresh "
                    + "full-access token (the old limited one stops working) and 10 one-time recovery "
                    + "codes - shown once, only their hashes are stored."
    )
    @PostMapping(ApiPaths.Dev.DEV_USER_ME_2FA_CONFIRM)
    public ApiResponse<DevTwoFactorConfirmResponse> confirmTwoFactor(
            @AuthenticationPrincipal DevUserPrincipal principal,
            @Valid @RequestBody DevTwoFactorConfirmRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                devAuthCommandService.confirmTwoFactor(principal.getDevUserId(), request.code(), httpRequest.getRemoteAddr())
        );
    }

    @Operation(
            summary = "Reset another dev-panel account's two-factor",
            description = "Clears the authenticator secret and recovery codes (lost phone and codes). The "
                    + "account's existing tokens stop working and it must enrol again on next login."
    )
    @PatchMapping(ApiPaths.Dev.DEV_USER_RESET_2FA)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> resetTwoFactor(
            @Parameter(description = "Internal id of the dev-panel account.", required = true)
            @PathVariable @Positive Long id
    ) {
        devAuthCommandService.resetTwoFactor(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }
}
