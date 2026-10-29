package uz.uzinfocom.app.integration.lis.client.dto;

/**
 * The unit LIS's {@code sampleUnit} field accepts, paired with
 * {@code sampleWeight} — confirmed against LIS's public
 * {@code SelectionActItemRequest} schema. Distinct from our own
 * {@code SampleQtUnit}/{@code SampleVolumeUnit}, which have values LIS does
 * not recognize (e.g. {@code MILLIGRAM}, {@code CUBIC_METER}), so those are
 * converted rather than passed through as-is.
 */
public enum LisSampleUnit {
    GRAM,
    KILOGRAM,
    MILLILITER,
    LITER
}
