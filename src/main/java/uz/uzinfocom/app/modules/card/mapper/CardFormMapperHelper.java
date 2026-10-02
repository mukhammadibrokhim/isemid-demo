package uz.uzinfocom.app.modules.card.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardFormResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DateInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DiagnosisInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ReportInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581DiagnosisInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581IncidentInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581ReportInfo;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Builds the read-only {@link CardFormResponse} block for the 5 card subtype
 * detail mappers — the fields a card's print/entry form shows straight from
 * its owning form058/form0581 case (diagnosis, report dates, sender), in one
 * shape for both form types. Kept apart from {@link CardCaseFieldMapperHelper}
 * because, unlike that one, this needs lookups (localized organization and
 * ICD-10 names).
 */
@Component
@RequiredArgsConstructor
public class CardFormMapperHelper {

    private final OrganizationMappingHelper organizationMappingHelper;
    private final Icd10LookupService icd10LookupService;

    @Named("resolveCardForm")
    public CardFormResponse resolveCardForm(Card card) {
        if (card.getForm058() != null) {
            return fromForm058(card.getForm058());
        }
        return card.getForm0581() != null ? fromForm0581(card.getForm0581()) : null;
    }

    private CardFormResponse fromForm058(Form058 form) {
        Form058DiagnosisInfo diagnosis = form.getDiagnosisInfo();
        Form058DateInfo dateInfo = form.getDateInfo();
        Form058ReportInfo reportInfo = form.getReportInfo();

        return build(
                form.getId(), CaseFormType.FORM058, form.getCreatedAt(),
                dateInfo != null ? dateInfo.getInitialReportDateTime() : null,
                diagnosis != null ? diagnosis.getIcd10Code() : null,
                diagnosis != null ? diagnosis.getIcd10Name() : null,
                diagnosis != null ? diagnosis.getFinalIcd10Code() : null,
                diagnosis != null ? diagnosis.getFinalIcd10Name() : null,
                form.getSenderOrganizationId(),
                reportInfo != null ? reportInfo.getNotifierFullName() : null
        );
    }

    /**
     * Form 058-1 has no {@code initialReportDateTime} of its own — the
     * closest equivalent is when the message was sent ({@code messageSentAt}),
     * falling back to the victim's visit to the medical facility.
     */
    private CardFormResponse fromForm0581(Form0581 form) {
        Form0581DiagnosisInfo diagnosis = form.getDiagnosisInfo();
        Form0581IncidentInfo incidentInfo = form.getIncidentInfo();
        Form0581ReportInfo reportInfo = form.getReportInfo();

        LocalDateTime initialReportDateTime = reportInfo != null ? reportInfo.getMessageSentAt() : null;
        if (initialReportDateTime == null && incidentInfo != null) {
            initialReportDateTime = incidentInfo.getDpuVisitDateTime();
        }

        return build(
                form.getId(), CaseFormType.FORM0581, form.getCreatedAt(),
                initialReportDateTime,
                diagnosis != null ? diagnosis.getIcd10Code() : null,
                diagnosis != null ? diagnosis.getIcd10Name() : null,
                diagnosis != null ? diagnosis.getFinalIcd10Code() : null,
                diagnosis != null ? diagnosis.getFinalIcd10Name() : null,
                form.getSenderOrganizationId(),
                reportInfo != null ? reportInfo.getNotifierFullName() : null
        );
    }

    private CardFormResponse build(
            Long id, CaseFormType formType, Instant createdAt, LocalDateTime initialReportDateTime,
            String icd10Code, String storedIcd10Name, String finalIcd10Code, String storedFinalIcd10Name,
            Long senderOrganizationId, String notifierFullName
    ) {
        // The name stored on the form is whatever language the sender typed it
        // in — prefer the catalog's localized name, keep the stored one as fallback.
        Map<String, String> localizedNames = icd10LookupService.resolveNames(
                Stream.of(icd10Code, finalIcd10Code).filter(Objects::nonNull).distinct().toList()
        );

        return new CardFormResponse(
                id, formType, createdAt, initialReportDateTime,
                icd10Code, localizedName(localizedNames, icd10Code, storedIcd10Name),
                finalIcd10Code, localizedName(localizedNames, finalIcd10Code, storedFinalIcd10Name),
                senderOrganizationId,
                organizationMappingHelper.activeOrganizationNameByIdOrNull(senderOrganizationId),
                notifierFullName,
                null
        );
    }

    private String localizedName(Map<String, String> localizedNames, String code, String storedName) {
        String localized = code != null && localizedNames != null ? localizedNames.get(code) : null;
        return localized != null ? localized : storedName;
    }
}
