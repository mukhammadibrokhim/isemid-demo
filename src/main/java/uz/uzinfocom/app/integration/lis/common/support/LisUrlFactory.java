package uz.uzinfocom.app.integration.lis.common.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.app.integration.lis.common.properties.LisProperties;

import java.net.URI;

/**
 * Builds every LIS URL from configured base/path properties via
 * {@link UriComponentsBuilder}, so path variables and query values are
 * properly encoded.
 *
 * <p>This deliberately replaces the previous {@code String.format}-style
 * {@code "/api/lis/labs/%s/acts/%s?allowedDuplicate=%s"} constants: those
 * hard-coded the host-relative shape, skipped encoding, and put the API key
 * at the call site. Everything is now one place and one convention.
 */
@Component
@RequiredArgsConstructor
public class LisUrlFactory {

    private static final String KEY_PARAM = "key";
    private static final String SENDER_ACT_NUMBER_PARAM = "senderActNumber";
    private static final String FORCE_PARAM = "force";
    private static final String RESEARCH_CODE_PARAM = "researchCode";

    private final LisProperties properties;

    /**
     * Act submission endpoint.
     *
     * @param labId           LIS laboratory the act is addressed to
     * @param senderActNumber our own act id, echoed back by LIS on the
     *                        callback so we can correlate the result
     * @param force           allow LIS to accept a duplicate of an act it has
     *                        already seen under the same sender act number
     */
    public URI createAct(Long labId, Long senderActNumber, boolean force) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(properties.endpoints().createAct())
                .queryParam(KEY_PARAM, properties.apiKey())
                .queryParam(SENDER_ACT_NUMBER_PARAM, senderActNumber)
                .queryParam(FORCE_PARAM, force)
                .encode()
                .buildAndExpand(labId)
                .toUri();
    }

    /**
     * Research-type lookup, resolving the LIS act-template id for a research
     * code (WATER/FOOD/SOIL).
     */
    public URI actTemplateId(String researchCode) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(properties.endpoints().actCode())
                .queryParam(KEY_PARAM, properties.apiKey())
                .queryParam(RESEARCH_CODE_PARAM, researchCode)
                .encode()
                .build()
                .toUri();
    }

    /**
     * The absolute URL LIS should post its result to for this act — handed to
     * LIS inside the push payload, never called by us. Built from
     * {@code callbackBaseUrl} (this app's public address) plus the act's own
     * id, so the callback is self-identifying.
     */
    public URI callbackUrl(Long actId) {
        return UriComponentsBuilder
                .fromUriString(properties.callbackBaseUrl())
                .path(uz.uzinfocom.app.shared.constants.api.ApiPaths.Act.ROOT)
                .path("/")
                .path(String.valueOf(actId))
                .path("/lis/callback")
                .encode()
                .build()
                .toUri();
    }
}
