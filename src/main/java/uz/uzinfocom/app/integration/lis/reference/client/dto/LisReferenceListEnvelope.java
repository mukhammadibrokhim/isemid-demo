package uz.uzinfocom.app.integration.lis.reference.client.dto;

import java.util.List;

/**
 * LIS's plain-list envelope shape ({@code {"data": [...]}}) — used by
 * {@code sesorgs}, {@code departments/{orgId}} and
 * {@code reference-dictionaries}, none of which are paginated.
 */
public record LisReferenceListEnvelope<T>(List<T> data) {
}
