package uz.uzinfocom.app.modules.reference.domain.enums;

/**
 * Where a {@code ref_population} row came from. {@link #SDMX} rows are
 * (re)written by the stat.uz sync; {@link #MANUAL} rows are admin
 * corrections and are never overwritten by a sync.
 */
public enum PopulationSource {
    SDMX,
    MANUAL
}
