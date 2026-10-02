package uz.uzinfocom.app.modules.form129.domain.enums;

/**
 * The Wright-Heddelson brucellosis agglutination test is the one Form 129
 * lab result with a third, "doubtful" outcome — every other test on the form
 * is a plain two-state {@code Form129TestOutcome}.
 */
public enum Form129WrightHeddelsonOutcome {
    NEGATIVE,
    DOUBTFUL,
    POSITIVE
}
