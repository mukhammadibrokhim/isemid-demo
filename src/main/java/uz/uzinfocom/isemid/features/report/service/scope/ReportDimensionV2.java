package uz.uzinfocom.isemid.features.report.service.scope;

import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;

/**
 * The granularity at which a location report is grouped.
 *
 * <p>Each dimension knows the {@link CatalogType} that supplies its rows, the
 * {@code pt_address} column that holds the matching geo code, and the legacy
 * {@code level} number used by {@code LocationReportDto.level}.</p>
 */
public enum ReportDimensionV2 {

    REGION(CatalogType.state, 2),
    DISTRICT(CatalogType.city, 3),
    NEIGHBOURHOOD(CatalogType.neighborhood, 4);

    private final CatalogType catalogType;
    private final int level;

    ReportDimensionV2(CatalogType catalogType, int level) {
        this.catalogType = catalogType;
        this.level = level;
    }

    public CatalogType catalogType() {
        return catalogType;
    }

    public int level() {
        return level;
    }
}
