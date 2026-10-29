package uz.uzinfocom.app.modules.card.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581AnimalOwnerInfo;
import uz.uzinfocom.app.modules.iam.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Resolves "whichever of form058/form0581 owns this card" for the 5 card
 * subtype detail mappers (Card161/174/175/205/CardTubeMapper) — a card
 * belongs to exactly one of the two, never both, so every subtype needs the
 * exact same branch. Mirrors {@code CardTableMapperHelper}'s equivalent
 * methods, which do the same thing for the table-row projection instead of
 * the full entity.
 * <p>
 * The {@code resolveCardXxxYyy} methods below give the response for a small
 * set of fields that the owning form058/form0581 case already carries
 * (diagnosis, animal, incident data) — read-only, GET-time-only fallback to
 * the case's value when the card's own field hasn't been filled in by the
 * assigned employee yet. Nothing is written back to the card row itself, so
 * there is no duplicated storage: once the employee fills the card field in
 * via {@code update}, their value wins over the case's.
 */
@Component
public class CardCaseFieldMapperHelper {

    @Named("resolveFormId")
    public Long resolveFormId(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getId();
        }
        return card.getForm0581() != null ? card.getForm0581().getId() : null;
    }

    @Named("resolveFormType")
    public CaseFormType resolveFormType(Card card) {
        if (card.getForm058() != null) {
            return CaseFormType.FORM058;
        }
        return card.getForm0581() != null ? CaseFormType.FORM0581 : null;
    }

    @Named("resolveCardTubeIcd10Code")
    public String resolveCardTubeIcd10Code(CardTube cardTube) {
        Form058 form058 = cardTube.getForm058();
        return firstNonNull(cardTube.getIcd10Code(), form058 != null && form058.getDiagnosisInfo() != null ? form058.getDiagnosisInfo().getIcd10Code() : null);
    }

    @Named("resolveCardTubeIcd10Name")
    public String resolveCardTubeIcd10Name(CardTube cardTube) {
        Form058 form058 = cardTube.getForm058();
        return firstNonNull(cardTube.getIcd10Name(), form058 != null && form058.getDiagnosisInfo() != null ? form058.getDiagnosisInfo().getIcd10Name() : null);
    }

    @Named("resolveCardTubeDgIcd10Code")
    public String resolveCardTubeDgIcd10Code(CardTube cardTube) {
        Form058 form058 = cardTube.getForm058();
        return firstNonNull(cardTube.getDgIcd10Code(), form058 != null && form058.getDiagnosisInfo() != null ? form058.getDiagnosisInfo().getFinalIcd10Code() : null);
    }

    @Named("resolveCardTubeDgIcd10Name")
    public String resolveCardTubeDgIcd10Name(CardTube cardTube) {
        Form058 form058 = cardTube.getForm058();
        return firstNonNull(cardTube.getDgIcd10Name(), form058 != null && form058.getDiagnosisInfo() != null ? form058.getDiagnosisInfo().getFinalIcd10Name() : null);
    }

    /**
     * Card174's diagnosis fields ({@code icd10Code}/{@code icd10Name}/{@code
     * humanPrimaryDiagnosis}) are not editable on the card — unlike the other
     * {@code resolveCardXxxYyy} methods here, the owning case's initial
     * diagnosis always wins, so the card can never disagree with its
     * notification. The card's own column is only a fallback for legacy rows
     * whose case carries no value.
     */
    @Named("resolveCard174Icd10Code")
    public String resolveCard174Icd10Code(Card174 card174) {
        return firstNonNull(caseIcd10Code(card174), card174.getIcd10Code());
    }

    @Named("resolveCard174Icd10Name")
    public String resolveCard174Icd10Name(Card174 card174) {
        return firstNonNull(caseIcd10Name(card174), card174.getIcd10Name());
    }

    @Named("resolveCard174HumanPrimaryDiagnosis")
    public String resolveCard174HumanPrimaryDiagnosis(Card174 card174) {
        return firstNonNull(caseIcd10Name(card174), card174.getHumanPrimaryDiagnosis());
    }

    private String caseIcd10Code(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getDiagnosisInfo() != null ? card.getForm058().getDiagnosisInfo().getIcd10Code() : null;
        }
        Form0581 form0581 = card.getForm0581();
        return form0581 != null && form0581.getDiagnosisInfo() != null ? form0581.getDiagnosisInfo().getIcd10Code() : null;
    }

    private String caseIcd10Name(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getDiagnosisInfo() != null ? card.getForm058().getDiagnosisInfo().getIcd10Name() : null;
        }
        Form0581 form0581 = card.getForm0581();
        return form0581 != null && form0581.getDiagnosisInfo() != null ? form0581.getDiagnosisInfo().getIcd10Name() : null;
    }

    /**
     * "Date the case information was obtained" — the notification's initial
     * report date. Form 058-1 has no such field, so the same substitute as
     * {@code CardFormMapperHelper} is used: when the message was sent,
     * falling back to the victim's visit to the medical facility.
     */
    @Named("resolveCard174DataObtainedDate")
    public LocalDate resolveCard174DataObtainedDate(Card174 card174) {
        return firstNonNull(card174.getDataObtainedDate(), toLocalDate(caseInitialReportDateTime(card174)));
    }

    @Named("resolveCard174LocationOfEvent")
    public String resolveCard174LocationOfEvent(Card174 card174) {
        Form0581 form0581 = card174.getForm0581();
        return firstNonNull(card174.getLocationOfEvent(),
                form0581 != null && form0581.getIncidentInfo() != null ? form0581.getIncidentInfo().getInjuryAddress() : null);
    }

    /**
     * Not case data, but the same "suggest until the employee fills it in"
     * idea: the employees the card is assigned to are the ones who carry out
     * the investigation.
     */
    @Named("resolveCard174Inspectors")
    public String resolveCard174Inspectors(Card174 card174) {
        if (card174.getInspectors() != null) {
            return card174.getInspectors();
        }
        String assigned = card174.getUsers().stream()
                .map(User::getFullName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::strip)
                .sorted()
                .collect(Collectors.joining(", "));
        return assigned.isEmpty() ? null : assigned;
    }

    private LocalDateTime caseInitialReportDateTime(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getDateInfo() != null ? card.getForm058().getDateInfo().getInitialReportDateTime() : null;
        }
        Form0581 form0581 = card.getForm0581();
        if (form0581 == null) {
            return null;
        }
        LocalDateTime messageSentAt = form0581.getReportInfo() != null ? form0581.getReportInfo().getMessageSentAt() : null;
        if (messageSentAt != null) {
            return messageSentAt;
        }
        return form0581.getIncidentInfo() != null ? form0581.getIncidentInfo().getDpuVisitDateTime() : null;
    }

    @Named("resolveCard174AnimalType")
    public String resolveCard174AnimalType(Card174 card174) {
        Form0581 form0581 = card174.getForm0581();
        return firstNonNull(card174.getAnimalType(),
                form0581 != null && form0581.getAnimalInfo() != null ? form0581.getAnimalInfo().getAnimalType() : null);
    }

    @Named("resolveCard174AnimalOwner")
    public String resolveCard174AnimalOwner(Card174 card174) {
        Form0581 form0581 = card174.getForm0581();
        return firstNonNull(card174.getAnimalOwner(), form0581 != null ? fullName(form0581.getAnimalOwnerInfo()) : null);
    }

    @Named("resolveCard205Icd10Code")
    public String resolveCard205Icd10Code(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getIcd10Code(), form0581 != null && form0581.getDiagnosisInfo() != null ? form0581.getDiagnosisInfo().getIcd10Code() : null);
    }

    @Named("resolveCard205Icd10Name")
    public String resolveCard205Icd10Name(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getIcd10Name(), form0581 != null && form0581.getDiagnosisInfo() != null ? form0581.getDiagnosisInfo().getIcd10Name() : null);
    }

    @Named("resolveCard205AnimalType")
    public String resolveCard205AnimalType(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getAnimalType(), form0581 != null && form0581.getAnimalInfo() != null ? form0581.getAnimalInfo().getAnimalType() : null);
    }

    @Named("resolveCard205DateOfBiteOccurrence")
    public LocalDate resolveCard205DateOfBiteOccurrence(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getDateOfBiteOccurrence(), form0581 != null && form0581.getIncidentInfo() != null ? toLocalDate(form0581.getIncidentInfo().getInjuryDateTime()) : null);
    }

    @Named("resolveCard205AddressOfBiteOccurrence")
    public String resolveCard205AddressOfBiteOccurrence(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getAddressOfBiteOccurrence(), form0581 != null && form0581.getIncidentInfo() != null ? form0581.getIncidentInfo().getInjuryAddress() : null);
    }

    @Named("resolveCard205FullNameOfAnimalOwner")
    public String resolveCard205FullNameOfAnimalOwner(Card205 card205) {
        Form0581 form0581 = card205.getForm0581();
        return firstNonNull(card205.getFullNameofAnimalOwner(), form0581 != null ? fullName(form0581.getAnimalOwnerInfo()) : null);
    }

    private <T> T firstNonNull(T ownValue, T caseFallback) {
        return ownValue != null ? ownValue : caseFallback;
    }

    private LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    private String fullName(Form0581AnimalOwnerInfo ownerInfo) {
        if (ownerInfo == null) {
            return null;
        }
        String joined = String.join(" ", nonBlank(ownerInfo.getOwnerLastName()), nonBlank(ownerInfo.getOwnerFirstName()), nonBlank(ownerInfo.getOwnerMiddleName()))
                .trim().replaceAll(" {2,}", " ");
        return joined.isEmpty() ? null : joined;
    }

    private String nonBlank(String value) {
        return value != null ? value : "";
    }
}
