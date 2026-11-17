package uz.uzinfocom.app.platform.persistence.audit;

import uz.uzinfocom.app.modules.iam.domain.Organization;

public interface CurrentAuditProvider {

    Long currentUserId();

    Organization currentOrganization();
}