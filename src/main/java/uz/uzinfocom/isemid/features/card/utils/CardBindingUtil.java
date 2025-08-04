package uz.uzinfocom.isemid.features.card.utils;

import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;

public class CardBindingUtil {
    public static void bindChildEntities(Card161 card) {
        if (card.getVaccinations() != null) {
            card.getVaccinations().forEach(v -> v.setCard161(card));
        }

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
        if (card.getInfectionMonitoring() != null) {
            card.getInfectionMonitoring().forEach(item -> item.setCard174(card));
        }
        if (card.getOutbreakControlMeasures() != null) {
            card.getOutbreakControlMeasures().forEach(item -> item.setCard174(card));
        }
    }

    public static void bindChildEntities(Card205 card) {
        if (card.getInfoBittenPeople() != null) {
            card.getInfoBittenPeople().forEach(item -> item.setCard205(card));
        }
        if (card.getInfoOtherBittenAnimal() != null) {
            card.getInfoOtherBittenAnimal().forEach(item -> item.setCard205(card));
        }
        if (card.getInfoAbtAnimalBittenPeople() != null) {
            card.getInfoAbtAnimalBittenPeople().forEach(item -> item.setCard205(card));
        }
    }

    public static void bindChildEntities(CardTube card) {
        if (card.getPreMBTChestXRay() != null) {
            card.getPreMBTChestXRay().forEach(xRay -> xRay.setCardTube(card));
        }
        if (card.getPreviousTBHistory() != null) {
            card.getPreviousTBHistory().forEach(history -> history.setCardTube(card));
        }
        if (card.getPossibleInfectionSources() != null) {
            card.getPossibleInfectionSources().forEach(possible -> possible.setCardTube(card));
        }
        if (card.getContactMonitoringList() != null) {
            card.getContactMonitoringList().forEach(contact -> contact.setCardTube(card));
        }
    }
}
