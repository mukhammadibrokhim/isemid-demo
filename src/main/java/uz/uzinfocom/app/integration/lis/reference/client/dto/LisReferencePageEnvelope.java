package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * LIS's paginated envelope shape ({@code {"data": {"list": [...], "total": N}}}).
 */
public record LisReferencePageEnvelope<T>(LisReferencePage<T> data) {
}
