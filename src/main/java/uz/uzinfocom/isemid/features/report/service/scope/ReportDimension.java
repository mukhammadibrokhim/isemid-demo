package uz.uzinfocom.isemid.features.report.service.scope;

import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;

/**
 * The granularity at which a location report is grouped.
 *
 * <p>Each dimension knows the {@link CatalogType} that supplies its rows, the
 * {@code pt_address} column that holds the matching geo code, and the legacy
 * {@code level} number used by {@code LocationReportDto.level}.</p>
 */
public enum ReportDimension {

    REGION(CatalogType.state, "state_code", 2),
    DISTRICT(CatalogType.city, "city_code", 3),
    NEIGHBOURHOOD(CatalogType.neighborhood, "neighborhood_code", 4);

    private final CatalogType catalogType;
    private final String addressColumn;
    private final int level;

    ReportDimension(CatalogType catalogType, String addressColumn, int level) {
        this.catalogType = catalogType;
        this.addressColumn = addressColumn;
        this.level = level;
    }

    public CatalogType catalogType() {
        return catalogType;
    }

    /** {@code pt_address} column to match against {@code cls_catalog.code} and group on. */
    public String addressColumn() {
        return addressColumn;
    }

    public int level() {
        return level;
    }
}
