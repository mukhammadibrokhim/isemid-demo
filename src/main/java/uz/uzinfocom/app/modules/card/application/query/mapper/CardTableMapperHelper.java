package uz.uzinfocom.app.modules.card.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;

/**
 * Resolves locale display names for {@link CardType}/{@link CardStatus} —
 * every other enum in this codebase is returned raw and localized
 * client-side, but the Card table view was explicitly asked to resolve
 * these server-side via {@code card.type.*}/{@code card.status.*} message
 * keys. Also resolves the "whichever of form058/form0581 owns this card"
 * fields (formId/formType/organizationId/organizationName/patient) — a card
 * has exactly one owning case, never both, so these all branch on the same
 * check.
 */
@Component
@RequiredArgsConstructor
public class CardTableMapperHelper {

    private final MessageResolver messageResolver;
    private final OrganizationMappingHelper organizationMappingHelper;

    @Named("cardTypeName")
    public String cardTypeName(CardType cardType) {
        return cardType == null ? null : messageResolver.resolve("card.type." + cardType.name());
    }

    @Named("cardStatusName")
    public String cardStatusName(CardStatus status) {
        return status == null ? null : messageResolver.resolve("card.status." + status.name());
    }

    @Named("resolveFormId")
    public Long resolveFormId(CardTableProjection projection) {
        if (projection.getForm058() != null) {
            return projection.getForm058().getId();
        }
        return projection.getForm0581() != null ? projection.getForm0581().getId() : null;
    }

    @Named("resolveFormType")
    public CaseFormType resolveFormType(CardTableProjection projection) {
        if (projection.getForm058() != null) {
            return CaseFormType.FORM058;
        }
        return projection.getForm0581() != null ? CaseFormType.FORM0581 : null;
    }

    @Named("resolveOrganizationId")
    public Long resolveOrganizationId(CardTableProjection projection) {
        if (projection.getForm058() != null) {
            return projection.getForm058().getReceiverOrganizationId();
        }
        return projection.getForm0581() != null ? projection.getForm0581().getReceiverOrganizationId() : null;
    }

    @Named("resolveOrganizationName")
    public String resolveOrganizationName(CardTableProjection projection) {
        return organizationMappingHelper.activeOrganizationNameById(resolveOrganizationId(projection));
    }

    @Named("resolvePatientShort")
    public CardTableResponse.PatientShortResponse resolvePatientShort(CardTableProjection projection) {
        CardTableProjection.PatientRef patient = projection.getForm058() != null
                ? projection.getForm058().getPatient()
                : projection.getForm0581() != null ? projection.getForm0581().getPatient() : null;

        return patient == null ? null : new CardTableResponse.PatientShortResponse(
                patient.getId(), patient.getFirstName(), patient.getLastName()
        );
    }
}
