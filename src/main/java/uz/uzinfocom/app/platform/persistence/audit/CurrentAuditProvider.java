package uz.uzinfocom.app.platform.persistence.audit;

import uz.uzinfocom.app.platform.iam.domain.Organization;

public interface CurrentAuditProvider {

    Long currentUserId();

    Organization currentOrganization();
}