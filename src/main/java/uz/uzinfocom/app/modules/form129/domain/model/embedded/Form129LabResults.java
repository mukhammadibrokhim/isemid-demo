package uz.uzinfocom.app.modules.form129.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;

/**
 * The 13 boolean lab test results on Form 129 (RW, RPR/VDRL, RPGA, ELISA,
 * TPHA, Western blot, HBsAg, HBeAg, Anti-HBc IgG, Anti-HBc IgM, Anti-HBe,
 * Anti-HBs, qualitative PCR) plus the one three-state test (Wright-Heddelson
 * brucellosis agglutination) — each pairs an {@code outcome} with an
 * editable free-text {@code resultText} (e.g. a titre) populated when the
 * outcome is positive, per the source form's "тахрирлаш имконияти" note on
 * every positive-result field. Flat like {@code Form0581DiagnosisInfo}
 * rather than 14 nested embeddables — there is no natural grouping beyond
 * "one test, one outcome, one detail text".
 */
@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form129LabResults {

    @Enumerated(EnumType.STRING)
    @Column(name = "rw_outcome", length = 16)
    private Form129TestOutcome rwOutcome;

    @Column(name = "rw_result_text", length = 500)
    private String rwResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "rpr_vdrl_outcome", length = 16)
    private Form129TestOutcome rprVdrlOutcome;

    @Column(name = "rpr_vdrl_result_text", length = 500)
    private String rprVdrlResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "rpga_outcome", length = 16)
    private Form129TestOutcome rpgaOutcome;

    @Column(name = "rpga_result_text", length = 500)
    private String rpgaResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "elisa_outcome", length = 16)
    private Form129TestOutcome elisaOutcome;

    @Column(name = "elisa_result_text", length = 500)
    private String elisaResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "tpha_outcome", length = 16)
    private Form129TestOutcome tphaOutcome;

    @Column(name = "tpha_result_text", length = 500)
    private String tphaResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "western_blot_outcome", length = 16)
    private Form129TestOutcome westernBlotOutcome;

    @Column(name = "western_blot_result_text", length = 500)
    private String westernBlotResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "hbs_ag_outcome", length = 16)
    private Form129TestOutcome hbsAgOutcome;

    @Column(name = "hbs_ag_result_text", length = 500)
    private String hbsAgResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "hbe_ag_outcome", length = 16)
    private Form129TestOutcome hbeAgOutcome;

    @Column(name = "hbe_ag_result_text", length = 500)
    private String hbeAgResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "anti_hbc_igg_outcome", length = 16)
    private Form129TestOutcome antiHbcIgGOutcome;

    @Column(name = "anti_hbc_igg_result_text", length = 500)
    private String antiHbcIgGResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "anti_hbc_igm_outcome", length = 16)
    private Form129TestOutcome antiHbcIgMOutcome;

    @Column(name = "anti_hbc_igm_result_text", length = 500)
    private String antiHbcIgMResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "anti_hbe_outcome", length = 16)
    private Form129TestOutcome antiHbeOutcome;

    @Column(name = "anti_hbe_result_text", length = 500)
    private String antiHbeResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "anti_hbs_outcome", length = 16)
    private Form129TestOutcome antiHbsOutcome;

    @Column(name = "anti_hbs_result_text", length = 500)
    private String antiHbsResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "pcr_qualitative_outcome", length = 16)
    private Form129TestOutcome pcrQualitativeOutcome;

    @Column(name = "pcr_qualitative_result_text", length = 500)
    private String pcrQualitativeResultText;

    @Enumerated(EnumType.STRING)
    @Column(name = "wright_heddelson_outcome", length = 16)
    private Form129WrightHeddelsonOutcome wrightHeddelsonOutcome;

    @Column(name = "wright_heddelson_result_text", length = 500)
    private String wrightHeddelsonResultText;
}
