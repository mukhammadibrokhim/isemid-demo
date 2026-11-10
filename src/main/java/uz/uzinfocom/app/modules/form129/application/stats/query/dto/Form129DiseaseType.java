package uz.uzinfocom.app.modules.form129.application.stats.query.dto;

/**
 * Derived (not persisted) classification of a {@code Form129} row, replacing
 * the ICD-10 "top diagnosis" breakdown Form058/Form0581 dashboards have —
 * Form129 carries lab test outcomes instead of a diagnosis code. Bucketed off
 * one primary marker field per disease ({@code rwOutcome} for syphilis,
 * {@code hbsAgOutcome} for hepatitis B, {@code wrightHeddelsonOutcome} for
 * brucellosis) rather than the full 13-test battery, since each Form129 is
 * submitted for one disease's test panel.
 */
public enum Form129DiseaseType {
    SYPHILIS,
    HEPATITIS_B,
    BRUCELLOSIS,
    UNKNOWN
}
