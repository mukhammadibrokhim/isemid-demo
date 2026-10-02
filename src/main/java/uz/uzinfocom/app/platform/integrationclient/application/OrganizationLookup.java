package uz.uzinfocom.app.platform.integrationclient.application;

import java.util.UUID;

/**
 * Port for resolving an active organization's internal id and display name
 * from its UUID. {@code modules.iam} provides the implementation (it owns
 * the {@code Organization} lookup) — this interface is what lets
 * {@code platform.integrationclient} consume that without depending on
 * {@code modules.iam} directly.
 */
public interface OrganizationLookup {

    Long resolveActiveId(UUID organizationUuid);

    String activeOrganizationNameById(Long organizationId);
}
