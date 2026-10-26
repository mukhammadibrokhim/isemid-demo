package uz.uzinfocom.app.integration.inbound.common.web;

import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;

import java.util.UUID;

/**
 * A form addressed in the URL by either its numeric id or its uuid — exactly one of the two is set.
 */
public record InboundFormIdentifier(Long id, UUID uuid) {

    public static InboundFormIdentifier parse(String value) {
        if (value == null || value.isBlank()) {
            throw new InboundValidationException("integration.form-id.invalid");
        }
        String trimmed = value.trim();
        try {
            if (trimmed.indexOf('-') >= 0) {
                return new InboundFormIdentifier(null, UUID.fromString(trimmed));
            }
            return new InboundFormIdentifier(Long.parseLong(trimmed), null);
        } catch (IllegalArgumentException invalid) {
            throw new InboundValidationException("integration.form-id.invalid");
        }
    }
}
