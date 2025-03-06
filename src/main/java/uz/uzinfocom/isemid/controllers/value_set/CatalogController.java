package uz.uzinfocom.isemid.controllers.value_set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.value_set.ReqCatalog;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalog;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.List;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CATALOG;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;

@RestController
@RequestMapping(CATALOG)
@Tag(name = "Catalog", description = "Работа с каталогом данных")
@RequiredArgsConstructor
public class CatalogController {

    final CatalogService catalogService;

    //    @PreAuthorize("hasAnyAuthority('create_catalog')")
//    @Operation(tags = "Catalog")
    @PostMapping
    public ApiResponse<Catalog> createCatalog(@Valid @RequestBody ReqCatalog reqCatalog) {
        return catalogService.createCatalog(reqCatalog);
    }

    @Operation(
            summary = "Получить все каталоги",
            description = """
                    Получает список всех каталогов с возможностью фильтрации:
                    - Параметры фильтрации включают тип, код, родительский код, и различные названия (например, на русском, узбекском, кириллицей).
                    - Пагинация доступна с параметрами `page` и `size`.
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации"),
                    @Parameter(name = "size", description = "Количество элементов на странице для пагинации"),
                    @Parameter(name = "type", description = "Тип каталога для фильтрации"),
                    @Parameter(name = "code", description = "Код каталога для фильтрации"),
                    @Parameter(name = "parentCode", description = "Родительский код для фильтрации"),
                    @Parameter(name = "nameRu", description = "Название на русском языке для фильтрации"),
                    @Parameter(name = "nameUz", description = "Название на узбекском языке для фильтрации"),
                    @Parameter(name = "nameUzCyril", description = "Название на узбекском языке (кириллицей) для фильтрации"),
                    @Parameter(name = "nameKaa", description = "Название на языке Kaa для фильтрации"),
            })
    @GetMapping("/all")
    public ApiResponsePage getAllCatalogs(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameKaa,
            HttpServletRequest request
    ) {
        return catalogService.getAllCatalogs(page, size, type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa, request);
    }

    @Operation(
            summary = "Получить все типы каталогов",
            description = "Получает список всех типов каталогов, доступных в системе.")
    @GetMapping("/types")
    public ApiResponse<List<RespCatalog>> getCatalogs() {
        String lang = "";
        return catalogService.findAllByType(CatalogType.catalogType, lang);
    }

    @Operation(
            summary = "Обновить каталог",
            description = "Обновляет существующий каталог по его ID. Передайте данные каталога для обновления.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога для обновления", required = true)})
    @PutMapping("/{id}")
    public ApiResponse<Catalog> updateCatalog(@PathVariable long id, @RequestBody ReqCatalog reqCatalog) {
        return catalogService.updateCatalogById(id, reqCatalog);
    }

//    @Operation(tags = "Catalog")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCatalog(@PathVariable long id) {
        return catalogService.deleteById(id);
    }

    @Operation(
            summary = "Получить все состояния",
            description = "Получает список всех доступных состояний из каталога.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/state")
    public ApiResponse<List<RespCatalog>> getCatalogState(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.state, lang);
    }

    @Operation(
            summary = "Получить все города по родительскому коду",
            description = "Получает список всех городов на основе родительского кода для фильтрации.",
            parameters = {
                    @Parameter(name = "parentCode", description = "Родительский код для фильтрации городов", required = true),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/city")
    public ApiResponse<List<RespCatalog>> getCatalogCity(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode, lang);
    }

    @Operation(
            summary = "Получить все типы идентификаторов",
            description = "Получает список всех доступных типов идентификаторов.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/identifier-type")
    public ApiResponse<List<RespCatalog>> getIdentifierType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.identifierType, lang);
    }

    @Operation(
            summary = "Получить все полы",
            description = "Получает список всех доступных полов.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/gender")
    public ApiResponse<List<RespCatalog>> getGender(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.gender, lang);
    }

    @Operation(
            summary = "Получить все страны",
            description = "Получает список всех стран.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации стран."),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/country")
    public ApiResponse<List<RespCatalog>> getCountry(
            @RequestHeader("Accept-Language") String lang,
            @RequestParam(value = "search", required = false) String search

    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.country, lang, search);
    }

    @Operation(
            summary = "Получить национальности по родительскому ID",
            description = "Получает список национальностей.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации национальности."),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/nationality")
    public ApiResponse<List<RespCatalog>> getNationality(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.nationality, lang, search);
    }

    @Operation(
            summary = "Получить все семейные положения",
            description = "Возвращает список всех типов семейных положений, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/marital-status")
    public ApiResponse<List<RespCatalog>> getMaritalStatus(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.maritalStatus, lang);
    }

    @Operation(
            summary = "Получить все статусы проживания",
            description = "Возвращает список всех типов статусов проживания, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/residential-status")
    public ApiResponse<List<RespCatalog>> getResidentialStatus(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.residentialStatus, lang);
    }

    @Operation(
            summary = "Получить тип адреса",
            description = "Возвращает список всех типов адресов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/address-type")
    public ApiResponse<List<RespCatalog>> getAddressType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.addressType, lang);
    }

    @GetMapping("/population-type")
    @Operation(
            summary = "Получить тип населённого пункта",
            description = "Возвращает список всех типов населённых пунктов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    public ApiResponse<List<RespCatalog>> getPopulationType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.populationType, lang);
    }

//    @Operation(tags = "Catalog Patient")
    @GetMapping("/positions")
    public ApiResponse<List<RespCatalog>> getPositions(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.position, lang);
    }

//    @Operation(tags = "Catalog Patient")
    @GetMapping("/levels")
    public ApiResponse<?> getLevels(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.level, lang);
    }

//    @Operation(tags = "Catalog From058")
    @GetMapping("/forms")
    public ApiResponse<?> getCards(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.formType, lang);
    }

//    @Operation(tags = "Catalog From058")
    @GetMapping("/disease-place")
    public ApiResponse<List<RespCatalog>> getDeceasePlace(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.diseasePlace, lang);
    }

//    @Operation(tags = "Catalog Patient")
    @GetMapping("/category")
    public ApiResponse<?> getCategory(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.category, lang);
    }

//    @Operation(tags = "Catalog Patient")
    @GetMapping("/profession")
    public ApiResponse<List<RespCatalog>> getProfession(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findByProfessionTypeAndSearch(CatalogType.profession, lang, search);
    }

//    @Operation(tags = "Catalog From058")
    @GetMapping("/defined-disease")
    public ApiResponse<?> definedDisease(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.definedDisease, lang);
    }

//    @Operation(tags = "Catalog Patient")
    @GetMapping("/neighborhood")
    public ApiResponse<List<RespCatalog>> getCatalogNeighborhood(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode, lang);

    }
    //Catalog 175
//    @Operation(tags = "Catalog Card175")
    @GetMapping("/where-patient-come")
    public ApiResponse<?> wherePatientCome(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.wherePatientCome, lang);

    }

    @GetMapping("/hospital-discharge")
    public ApiResponse<?> hospitalDischargeStatus(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.hospitalDischargeStatus, lang);

    }

    @GetMapping("/transport-type")
    public ApiResponse<?> transportType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.transportType, lang);

    }
    @GetMapping("/reason-of-leaving-home")
    public ApiResponse<?> reasonOfLeavingHome(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.reasonOfLeavingHome, lang);

    }
    @GetMapping("/reason-of-hospitalization")
    public ApiResponse<?> reasonOfLateHospitalization(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.reasonOfLateHospitalization, lang);

    }
    @GetMapping("/diagnosis-confirmed")
    public ApiResponse<?> diagnosisConfirmed(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.diagnosisConfirmed, lang);

    }
    @GetMapping("/information-about-vaccination")
    public ApiResponse<?> informationAboutVaccination(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.informationAboutVaccination, lang);

    }
    @GetMapping("/initial-diagnosis")
    public ApiResponse<?> initialDiagnosis(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.initialDiagnosis, lang);

    }
    @GetMapping("/patient-identified")
    public ApiResponse<?> patientIdentified(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.patientIdentified, lang);

    }
    @GetMapping("/information-about-immediate-prevention")
    public ApiResponse<?> informationAboutImmediatePreventionAndAntirabicAid(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.informationAboutImmediatePreventionAndAntirabicAid, lang);

    }
    @GetMapping("/part-of-injury")
    public ApiResponse<?> partOfInjury(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.partOfInjury, lang);

    }
    @GetMapping("/relevance-of-disease")
    public ApiResponse<?> relevanceOfDiseaseToProfession(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.relevanceOfDiseaseToProfession, lang);

    }
    @GetMapping("/severity-of-illness")
    public ApiResponse<?> severityOfIllness(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.severityOfIllness, lang);
    }
    @GetMapping("/disease-spreader-type")
    public ApiResponse<?> diseaseSpreaderType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.diseaseSpreaderType, lang);

    }
    @GetMapping("/owner-of-disease-spreader")
    public ApiResponse<?> ownerOfDiseaseSpreader(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.ownerOfDiseaseSpreader, lang);

    }
    @GetMapping("/observation-result-animals")
    public ApiResponse<?> observationResultOfAnimals(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.observationResultOfAnimals, lang);

    }
    @GetMapping("/checking-diagnosis")
    public ApiResponse<?> checkingDiagnosis(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.checkingDiagnosis, lang);

    }
    @GetMapping("/disease-transmission-condition")
    public ApiResponse<?> diseaseTransmissionCondition(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.diseaseTransmissionCondition, lang);

    }
    @GetMapping("/pathogen-factor")
    public ApiResponse<?> pathogenMainFactor(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.pathogenMainFactor, lang);

    }
    @GetMapping("/taken-measures-from-refidence")
    public ApiResponse<?> takenMeasuresFromResidence(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.takenMeasuresFromResidence, lang);

    }
    @GetMapping("/delivery-method")
    public ApiResponse<?> deliveryMethod(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.transportType, lang);

    }
    @GetMapping("/home-stay-reason")
    public ApiResponse<?> homeStayExclusionReason(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.reasonOfLeavingHome, lang);

    }
    @GetMapping("/late-admission-reason")
    public ApiResponse<?> lateAdmissionReason(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.reasonOfLateHospitalization, lang);

    }
    @GetMapping("/diagnosis-verified")
    public ApiResponse<?> diagnosisVerified(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.diagnosisVerified, lang);

    }
    @GetMapping("/animal-category")
    public ApiResponse<?> animalCategory(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.animalCategory, lang);
    }
    @GetMapping("/bitten-animal-category")
    public ApiResponse<?> bittenAnimalCategory(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.animalCategory, lang);
    }
    @GetMapping("/dog-owner-compliance")
    public ApiResponse<?> dogOwnerCompliance(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.dogOwnerCompliance, lang);
    }
    @GetMapping("/condition-of-animal")
    public ApiResponse<?> conditionOfAnimal(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.conditionOfAnimal, lang);
    }
    @GetMapping("/animal-conservation")
    public ApiResponse<?> animalConservation(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.animalConservation, lang);
    }
    @GetMapping("/affected-animal-type")
    public ApiResponse<?> affectedAnimalType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.affectedAnimalType, lang);
    }
    @GetMapping("/animal-ownership")
    public ApiResponse<?> animalOwnership(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.ownerOfDiseaseSpreader, lang);
    }
    @GetMapping("/animal-disposal-method")
    public ApiResponse<?> animalDisposalMethod(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.animalDisposalMethod, lang);
    }
    @GetMapping("/deratization")
    public ApiResponse<?> deratization(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.deratization, lang);
    }
    @GetMapping("/quarantine-type")
    public ApiResponse<?> quarantineType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.quarantineType, lang);
    }
    @GetMapping("/disinfection-transmission-factors")
    public ApiResponse<?> disinfectionTransmissionFactors(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.disinfectionTransmissionFactors, lang);
    }
    @GetMapping("disease_factors")
    public ApiResponse<?> diseaseFactors(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.diseaseFactors, lang);
    }
    @GetMapping("processing-method")
    public ApiResponse<?> processingMethod(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.processingMethod, lang);
    }

    @GetMapping("/elimination-method")
    public ApiResponse<?> eliminationMethod(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.eliminationMethod, lang);
    }
    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/risk-factor")
    public ApiResponse<?> getRiskFactor(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.riskFactor, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/patient-detected")
    public ApiResponse<?> getDetected(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.detected, lang);
    }

//    @Operation(summary = "", tags = "Catalog Card161")
//    @GetMapping("/vaccination-verified")
//    public ApiResponse<?> getHoeStayReason(@RequestHeader("Accept-Language") String lang) {
//        return catalogService.findAllByType(CatalogType.vaccinationVerified, lang);
//    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/living-condition")
    public ApiResponse<?> getLivingCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.livingCondition, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/water-supply")
    public ApiResponse<?> getWaterSupply(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.waterSupply, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/liq-waste-type")
    public ApiResponse<?> getLqwT(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.liquidWaste, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/solid-waste-type")
    public ApiResponse<?> getSolidWaste(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.solidWaste, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/room-condition")
    public ApiResponse<?> getRoomCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.roomCondition, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/yard-condition")
    public ApiResponse<?> getYardCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.yardCondition, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/area-condition")
    public ApiResponse<?> getAreaCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.areaContidion, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/visit-objects")
    public ApiResponse<?> getVisitObj(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.visitetObj, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/prevent-measure")
    public ApiResponse<?> getPreventMeasure(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.preventMeasure, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/conducted-location")
    public ApiResponse<?> getConductedLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.counductedLoca, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/infection-location")
    public ApiResponse<?> getInfectionLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infectedLocation, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/probable-infection-location")
    public ApiResponse<?> getProbableInfectionLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.probableInfectionLoc, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/infection-source")
    public ApiResponse<?> getInfectionSource(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.notFoundInfection, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/infection-period")
    public ApiResponse<?> getInfectionPeriod(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infectionPeriod, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/animal-type")
    public ApiResponse<?> getAnimalType(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.animalType, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/main-probable-infection-factor")
    public ApiResponse<?> getMainFactor(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.mainFactor, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/infection-condition")
    public ApiResponse<?> getInfectionCond(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infeCondition, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/outbreak-infection")
    public ApiResponse<?> getOutBreak(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.outbreakInfection, lang);
    }

    @Operation(summary = "", tags = "Catalog Card161")
    @GetMapping("/case-status")
    public ApiResponse<?> getCase(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.caseStatus, lang);
    }
}
