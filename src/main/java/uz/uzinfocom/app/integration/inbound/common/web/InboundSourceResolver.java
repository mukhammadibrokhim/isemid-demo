package uz.uzinfocom.app.integration.inbound.common.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;

import java.util.Locale;

/**
 * Resolves the {@code source} recorded on a form created through the
 * inbound-integration API — the name of the external (or internal) system
 * that submitted it, taken from the {@code {source}} URL path segment (e.g.
 * {@code /integration/v1/dmed/form-058}), not a header: the caller identifies
 * itself as part of the endpoint it's calling, not via a separately-settable
 * value. For an integration-client caller this is additionally cross-checked
 * against the client's own registered source key
 * ({@link InboundCallerContext#requireMatchingSourceKey(String)}), so a
 * client can never attribute its submissions to a different system's name.
 */
@Component
public class InboundSourceResolver {

    private static final int MAX_SOURCE_LENGTH = 64;

    public String resolve(String source) {
        if (!StringUtils.hasText(source)) {
            throw new InboundValidationException("integration.source.required");
        }

        String normalized = source.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() > MAX_SOURCE_LENGTH) {
            throw new InboundValidationException("integration.source.too-long", MAX_SOURCE_LENGTH);
        }

        return normalized;
    }
}
