package uz.uzinfocom.app.modules.report.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

/**
 * Decides which organization a case is attributed to in the geography
 * hierarchy of a report: the one that <b>received</b> it or the one that
 * <b>created</b> (sent) it.
 * <p>
 * A territorial caller (ALL / REGION / DISTRICT scope — the SES branches and
 * above) wants a case counted where the receiving SES sits: a hospital in one
 * district notifying the SES of another must land in the receiving SES's
 * district. A caller limited to a single organization (a hospital/clinic) only
 * sees its own organization, whose cases it <i>sent</i>, so for that scope the
 * attribution stays on the sender — attributing to the receiver would show it
 * zeros.
 */
@Component
@RequiredArgsConstructor
public class ReportCaseAttribution {

    private static final String SENDER_COLUMN = "sender_organization_id";
    private static final String RECEIVER_COLUMN = "receiver_organization_id";

    private final OrganizationScopeResolver organizationScopeResolver;

    /** {@code true} when cases must be attributed to the receiver organization, {@code false} for the sender. */
    public boolean byReceiver() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional().orElse(null);
        if (currentOrganization == null) {
            return false;
        }
        return organizationScopeResolver.resolve(currentOrganization).mode() != OrganizationScopeMode.ORGANIZATION;
    }

    /**
     * Rewrites a report's native SQL so every case is attributed to the
     * receiver organization instead of the sender when {@link #byReceiver()}
     * holds. The whole token is renamed (select list, scope join, grouping and
     * outer alias alike), so the query keeps its shape and column order; every
     * report query keys its cases on {@code <alias>.sender_organization_id},
     * and {@code form058}, {@code form058_1} and {@code form_129} all carry a
     * matching {@code receiver_organization_id} column.
     */
    public String apply(String sql) {
        return byReceiver() ? sql.replace(SENDER_COLUMN, RECEIVER_COLUMN) : sql;
    }
}
