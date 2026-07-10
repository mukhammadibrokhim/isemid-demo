package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationAboutAnimaBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenAnimalsResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Карта 205 — карта эпидемиологического расследования случая укуса животным (полные сведения).")
public record Card205DetailResponse(
        @Schema(description = "Идентификатор карты.")
        Long id,

        @Schema(description = "Тип карты.")
        CardType type,

        @Schema(description = "Текущий статус карты в её жизненном цикле.")
        CardStatus status,

        @Schema(description = "Идентификатор формы №058, к которой привязана карта.")
        Long formId,

        @Schema(description = "Идентификатор супервайзера, назначившего карту.")
        Long assignedById,

        @Schema(description = "Комментарий супервайзера (например, причина отклонения при проверке).")
        String supervisorComment,

        @Schema(description = "Комментарий прикреплённого сотрудника (например, причина отказа от карты).")
        String attachedUserComment,

        @Schema(description = "Дата завершения заполнения карты.")
        LocalDate completedDate,

        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Дата эпидемиологического наблюдения.")
        LocalDate epidemiologicalObservationDate,

        @Schema(description = "Дата направления экстренного извещения в ветеринарную службу.")
        LocalDate veterinaryEmergencyInformationDate,

        @Schema(description = "Адрес места, где произошёл укус.")
        String addressOfBiteOccurrence,

        @Schema(description = "Дата укуса.")
        LocalDate dateOfBiteOccurrence,

        @Schema(description = "Наименование лечебно-профилактического учреждения.")
        String nameOfTreatmentPreventiveInstitution,

        @Schema(description = "Дата обращения в лечебно-профилактическое учреждение.")
        LocalDate dateOfTreatmentPreventiveInstitution,

        @Schema(description = "Список сведений о других лицах, пострадавших от укуса того же животного.")
        List<InformationOtherBittenPeopleResponse> infoBittenPeople,

        @Schema(description = "Вид животного, нанёсшего укус.")
        String animalType,

        @Schema(description = "Откуда появилось животное.")
        String whereAnimalComesFrom,

        @Schema(description = "Когда появилось животное.")
        String whenAnimalAppeared,

        @Schema(description = "Код состояния животного на момент наблюдения (по справочнику).")
        String conditionOfAnimalCode,

        @Schema(description = "Возраст животного.")
        Integer ageOfAnimal,

        @Schema(description = "Порода животного.")
        String breedOfAnimal,

        @Schema(description = "Окрас животного.")
        String colourOfAnimal,

        @Schema(description = "Индивидуальные (отличительные) признаки животного.")
        String individualSignsOfAnimal,

        @Schema(description = "Номер первичной ветеринарной справки о результатах наблюдения за животным.")
        Integer certificateNumberOfFirstVetResults,

        @Schema(description = "Дата выдачи первичной ветеринарной справки.")
        LocalDate issueDateOfFirstCertificate,

        @Schema(description = "Код способа содержания животного при наблюдении (по справочнику).")
        String animalConservationCode,

        @Schema(description = "Код положения/статуса пострадавшего от укуса (по справочнику).")
        String positionOfBittenVictimCode,

        @Schema(description = "Номер повторной ветеринарной справки о результатах наблюдения за животным.")
        Integer certificateNumberOfSecondVetResults,

        @Schema(description = "Дата выдачи повторной ветеринарной справки.")
        LocalDate issueDateOfSecondaryCertificate,

        @Schema(description = "Дата и время взятия биоматериала (шерсти/перьев) животного для исследования.")
        String dateTimeOfFeatherTaken,

        @Schema(description = "Ветеринарное учреждение, в котором зарегистрировано животное.")
        String petRegisteredVetDepartment,

        @Schema(description = "Код соблюдения владельцем животного установленных правил содержания (по справочнику).")
        String dogOwnerComplianceCode,

        @Schema(description = "Список сведений о других животных, пострадавших от того же источника заражения.")
        List<InformationOtherBittenAnimalsResponse> infoOtherBittenAnimal,

        @Schema(description = "Список сведений о владельцах животных, укушенных тем же источником заражения.")
        List<InformationAboutAnimaBittenPeopleResponse> infoAbtAnimalBittenPeople,

        @Schema(description = "Дополнительная информация по случаю.")
        String additionalInformation,

        @Schema(description = "ФИО владельца животного, нанёсшего укус.")
        String fullNameofAnimalOwner
) implements CardDetailResponse {
}
