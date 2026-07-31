package uz.uzinfocom.app.platform.auth.application.exception;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * The refresh token being exchanged was already blacklisted by a prior
 * {@code /v1/auth/logout/{provider}} call (see {@code TokenBlacklistService})
 * - rejected before it ever reaches the provider's token endpoint, otherwise
 * logout would be pointless: a logged-out caller could mint a fresh,
 * non-blacklisted access token via {@code /v1/auth/refresh/{provider}} using
 * the same refresh token.
 */
public class RevokedTokenException extends AppException {

    public RevokedTokenException(String providerKey) {
        super(ErrorCode.UNAUTHORIZED, "auth.login.token-revoked", providerKey);
    }
}
