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
    private static final String NAME_PARAM = "name";
    private static final String TYPE_PARAM = "type";
    private static final String CONDITIONS_TYPE = "CONDITIONS";

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
    /**
     * Organization catalog ({@code sesorgs}), optionally filtered by a
     * caller-supplied name fragment — LIS itself does the matching.
     */
    public URI sesorgs(String name) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(properties.endpoints().sesorgs())
                .queryParam(KEY_PARAM, properties.apiKey())
                .queryParam(NAME_PARAM, name == null ? "" : name)
                .encode()
                .build()
                .toUri();
    }

    /**
     * Departments (sub-organizations) of one LIS organization.
     */
    public URI departments(Long organizationId) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(properties.endpoints().departments())
                .queryParam(KEY_PARAM, properties.apiKey())
                .encode()
                .buildAndExpand(organizationId)
                .toUri();
    }

    /**
     * Storage/delivery/special condition dictionary — the only
     * {@code reference-dictionaries} type this app currently needs.
     */
    public URI conditions() {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(properties.endpoints().referenceDictionaries())
                .queryParam(KEY_PARAM, properties.apiKey())
                .queryParam(TYPE_PARAM, CONDITIONS_TYPE)
                .encode()
                .build()
                .toUri();
    }

    public URI professions() {
        return paginatedLookup(properties.endpoints().professions());
    }

    public URI researchTypes() {
        return paginatedLookup(properties.endpoints().researchTypes());
    }

    public URI categories() {
        return paginatedLookup(properties.endpoints().categories());
    }

    public URI itemTypes() {
        return paginatedLookup(properties.endpoints().itemTypes());
    }

    /**
     * The four LIS dictionary endpoints that are POST + a
     * {@code Pagination<Search>} body rather than a plain GET — the body
     * itself is built by the caller (see {@code LisReferenceClient}), this
     * only carries the {@code key} query parameter every LIS call needs.
     */
    private URI paginatedLookup(String path) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl())
                .path(path)
                .queryParam(KEY_PARAM, properties.apiKey())
                .encode()
                .build()
                .toUri();
    }

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
