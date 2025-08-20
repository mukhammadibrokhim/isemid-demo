package uz.uzinfocom.isemid.features.card.utils;

import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;

import java.util.Optional;

public class CardBindingUtil {
    public static void bindChildEntities(Card161 card) {
        if (card.getRiskFactors() != null) {
            card.getRiskFactors().forEach(rf -> rf.setCard161(card));
        }

        if (card.getPossibleInfectionSources() != null) {
            card.getPossibleInfectionSources().forEach(source -> source.setCard161(card));
        }

        if (card.getEnvironmentalSources() != null) {
            card.getEnvironmentalSources().forEach(env -> env.setCard161(card));
        }

        if (card.getEnvironmentalLabTests() != null) {
            card.getEnvironmentalLabTests().forEach(test -> test.setCard161(card));
        }

        if (card.getContactPersonDetails() != null) {
            card.getContactPersonDetails().forEach(contact -> contact.setCard161(card));
        }

        if (card.getScreenedGroups() != null) {
            card.getScreenedGroups().forEach(group -> group.setCard161(card));
        }

        if (card.getHomePreventiveMeasures() != null) {
            card.getHomePreventiveMeasures().forEach(measure -> measure.setCard161(card));
        }

        if (card.getOutbreakDisinfectionMeasures() != null) {
            card.getOutbreakDisinfectionMeasures().forEach(measure -> measure.setCard161(card));
        }
    }

    public static void bindChildEntities(Card174 card) {
        Optional.ofNullable(card.getMonitoring())
                .ifPresent(list -> list.forEach(item -> item.setCard174(card)));

        Optional.ofNullable(card.getControlMeasures())
                .ifPresent(list -> list.forEach(item -> item.setCard174(card)));

        Optional.ofNullable(card.getExternalSampleTests())
                .ifPresent(list -> list.forEach(item -> item.setCard174(card)));
    }


    public static void bindChildEntities(Card205 card) {
        if (card.getAnimalBiteVictims() != null) {
            card.getAnimalBiteVictims().forEach(item -> item.setCard205(card));
        }
        if (card.getOtherBittenAnimals() != null) {
            card.getOtherBittenAnimals().forEach(item -> item.setCard205(card));
        }
        if (card.getOtherBittenPersons() != null) {
            card.getOtherBittenPersons().forEach(item -> item.setCard205(card));
        }
    }

    public static void bindChildEntities(CardTube card) {
        if (card.getChestXrays() != null) {
            card.getChestXrays().forEach(xRay -> xRay.setCardTube(card));
        }
        if (card.getTbHistory() != null) {
            card.getTbHistory().forEach(history -> history.setCardTube(card));
        }
        if (card.getInfectionSources() != null) {
            card.getInfectionSources().forEach(possible -> possible.setCardTube(card));
        }
        if (card.getContactMonitoring() != null) {
            card.getContactMonitoring().forEach(contact -> contact.setCardTube(card));
        }
    }
}
