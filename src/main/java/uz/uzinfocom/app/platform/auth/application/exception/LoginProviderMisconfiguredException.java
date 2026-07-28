package uz.uzinfocom.app.platform.auth.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * The provider rejected our own client credentials or request shape - OAuth2
 * {@code error} of {@code invalid_client}, {@code unauthorized_client},
 * {@code invalid_request}, {@code unsupported_grant_type}, or {@code
 * invalid_scope}. Deliberately distinct from {@link
 * InvalidLoginCredentialsException}: this is not the caller's fault (a bad
 * username/password or an expired/reused code) - it's a misconfigured
 * client_id/client_secret/grant-type/scope on <em>our</em> side, or the
 * provider not actually allowing the grant we configured for it. The HTTP
 * response still reads as a generic upstream failure (same {@code
 * UPSTREAM_ERROR} the caller would see for a 5xx) - a frontend can't act on
 * "your client_id is wrong" any more than on "the server is down" - but the
 * distinct exception type and the logged upstream error code make this
 * immediately triageable as a backend config bug rather than routine user
 * error or transient network flakiness.
 */
public class LoginProviderMisconfiguredException extends AppException {

    public LoginProviderMisconfiguredException(String providerKey) {
        super(ErrorCode.UPSTREAM_ERROR, "auth.login.upstream-error", providerKey);
    }
}
