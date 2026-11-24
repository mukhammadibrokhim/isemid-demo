package uz.uzinfocom.app.modules.reference.application.geosync;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config for the e-government classifier registry (cs.egov.uz) partner API that feeds the
 * district / neighborhood sync. Overridable via {@code app.reference.egov-classifier.*} (or the
 * matching env vars) so a renamed classifier needs no redeploy. The access token is a secret -
 * it comes from {@code EGOV_CLASSIFIER_ACCESS_TOKEN} in {@code .env}, never from source.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.reference.egov-classifier")
public class EgovClassifierProperties {

    private String baseUrl = "https://cs.egov.uz";

    private String accessToken = "";

    /** "Mahalla yagona klassifikatori" - uzcad/1C codes, TIN, district SOATO per neighborhood. */
    private String neighborhoodClassifier = "035-3-004";

    /** Region + district SOATO list (Russian names only). */
    private String soatoClassifier = "035-1-002";

    /** Rows requested per page; the API served all 8 993 neighborhoods in one 10 000-row page. */
    private int pageLimit = 10000;
}
