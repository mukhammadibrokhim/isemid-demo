package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationAboutAnimaBittenPeopleRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenAnimalsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenPeopleRequest;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Карта 205 — карта эпидемиологического расследования случая укуса животным.")
public record Card205Request(
        @Schema(description = "Код диагноза по МКБ-10.")
        @Size(max = 64) String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        @Size(max = 500) String mkb10Name,

        @Schema(description = "Дата эпидемиологического наблюдения.")
        LocalDate epidemiologicalObservationDate,

        @Schema(description = "Дата направления экстренного извещения в ветеринарную службу.")
        LocalDate veterinaryEmergencyInformationDate,

        @Schema(description = "Адрес места, где произошёл укус.")
        @Size(max = 500) String addressOfBiteOccurrence,

        @Schema(description = "Дата укуса.")
        LocalDate dateOfBiteOccurrence,

        @Schema(description = "Наименование лечебно-профилактического учреждения.")
        @Size(max = 255) String nameOfTreatmentPreventiveInstitution,

        @Schema(description = "Дата обращения в лечебно-профилактическое учреждение.")
        LocalDate dateOfTreatmentPreventiveInstitution,

        @Schema(description = "Список сведений о других лицах, пострадавших от укуса того же животного.")
        @Valid List<InformationOtherBittenPeopleRequest> infoBittenPeople,

        @Schema(description = "Вид животного, нанёсшего укус.")
        @Size(max = 255) String animalType,

        @Schema(description = "Откуда появилось животное.")
        @Size(max = 500) String whereAnimalComesFrom,

        @Schema(description = "Когда появилось животное.")
        @Size(max = 255) String whenAnimalAppeared,

        @Schema(description = "Код состояния животного на момент наблюдения (по справочнику).")
        @Size(max = 64) String conditionOfAnimalCode,

        @Schema(description = "Возраст животного.")
        Integer ageOfAnimal,

        @Schema(description = "Порода животного.")
        @Size(max = 255) String breedOfAnimal,

        @Schema(description = "Окрас животного.")
        @Size(max = 255) String colourOfAnimal,

        @Schema(description = "Индивидуальные (отличительные) признаки животного.")
        @Size(max = 500) String individualSignsOfAnimal,

        @Schema(description = "Номер первичной ветеринарной справки о результатах наблюдения за животным.")
        Integer certificateNumberOfFirstVetResults,

        @Schema(description = "Дата выдачи первичной ветеринарной справки.")
        LocalDate issueDateOfFirstCertificate,

        @Schema(description = "Код способа содержания животного при наблюдении (по справочнику).")
        @Size(max = 64) String animalConservationCode,

        @Schema(description = "Код положения/статуса пострадавшего от укуса (по справочнику).")
        @Size(max = 64) String positionOfBittenVictimCode,

        @Schema(description = "Номер повторной ветеринарной справки о результатах наблюдения за животным.")
        Integer certificateNumberOfSecondVetResults,

        @Schema(description = "Дата выдачи повторной ветеринарной справки.")
        LocalDate issueDateOfSecondaryCertificate,

        @Schema(description = "Дата и время взятия биоматериала (шерсти/перьев) животного для исследования.")
        @Size(max = 255) String dateTimeOfFeatherTaken,

        @Schema(description = "Ветеринарное учреждение, в котором зарегистрировано животное.")
        @Size(max = 255) String petRegisteredVetDepartment,

        @Schema(description = "Код соблюдения владельцем животного установленных правил содержания (по справочнику).")
        @Size(max = 64) String dogOwnerComplianceCode,

        @Schema(description = "Список сведений о других животных, пострадавших от того же источника заражения.")
        @Valid List<InformationOtherBittenAnimalsRequest> infoOtherBittenAnimal,

        @Schema(description = "Список сведений о владельцах животных, укушенных тем же источником заражения.")
        @Valid List<InformationAboutAnimaBittenPeopleRequest> infoAbtAnimalBittenPeople,

        @Schema(description = "Дополнительная информация по случаю.")
        @Size(max = 1000) String additionalInformation,

        @Schema(description = "ФИО владельца животного, нанёсшего укус.")
        @Size(max = 255) String fullNameofAnimalOwner
) implements CardRequest {

    public Card205Request {
        infoBittenPeople = immutableCopy(infoBittenPeople);
        infoOtherBittenAnimal = immutableCopy(infoOtherBittenAnimal);
        infoAbtAnimalBittenPeople = immutableCopy(infoAbtAnimalBittenPeople);
    }

    @Override
    public CardType type() {
        return CardType.CARD205;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
