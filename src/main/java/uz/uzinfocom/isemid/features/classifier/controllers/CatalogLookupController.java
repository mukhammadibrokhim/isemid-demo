package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapper;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Catalog.ROOT)
@Tag(name = "Catalog Look UP")
@RequiredArgsConstructor
public class CatalogLookupController {
    private final CatalogService catalogService;
    private final MessageUtil messageUtil;
    private final CatalogMapper catalogMapper;

    @Operation(
            summary = "Получить все типы каталогов",
            description = "Получает список всех типов каталогов, доступных в системе.")
    @GetMapping("/types")
    public ApiResponse<?> getCatalogs(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.catalogType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все состояния",
            description = "Получает список всех доступных состояний из каталога.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/state")
    public ApiResponse<?> getCatalogState(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.state)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все города по родительскому коду",
            description = "Получает список всех городов на основе родительского кода для фильтрации.",
            parameters = {
                    @Parameter(name = "parentCode", description = "Родительский код для фильтрации городов", required = true),
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/city")
    public ApiResponse<?> getCatalogCity(
            @RequestParam String parentCode,
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все типы идентификаторов",
            description = "Получает список всех доступных типов идентификаторов.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/identifier-type")
    public ApiResponse<?> getIdentifierType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.identifierType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все полы",
            description = "Получает список всех доступных полов.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/gender")
    public ApiResponse<?> getGender(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.gender)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все страны",
            description = "Получает список всех стран.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации стран."),
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/country")
    public ApiResponse<?> getCountry(
            @RequestHeader(ACCEPT_LANGUAGE) String lang,
            @RequestParam(value = "search", required = false) String search

    ) {
        return ApiResponse.success(messageUtil, catalogService.findFirst20ByTypeAndSearch(CatalogType.country, search)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить национальности по родительскому ID",
            description = "Получает список национальностей.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации национальности."),
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/nationality")
    public ApiResponse<?> getNationality(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findFirst20ByTypeAndSearch(CatalogType.nationality, search)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все семейные положения",
            description = "Возвращает список всех типов семейных положений, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/marital-status")
    public ApiResponse<?> getMaritalStatus(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.maritalStatus)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить все статусы проживания",
            description = "Возвращает список всех типов статусов проживания, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/residential-status")
    public ApiResponse<?> getResidentialStatus(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.residentialStatus)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Operation(
            summary = "Получить тип адреса",
            description = "Возвращает список всех типов адресов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/address-type")
    public ApiResponse<?> getAddressType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.addressType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/population-type")
    @Operation(
            summary = "Получить тип населённого пункта",
            description = "Возвращает список всех типов населённых пунктов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = ACCEPT_LANGUAGE, description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    public ApiResponse<?> getPopulationType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.populationType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/positions")
    public ApiResponse<?> getPositions(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.position)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/levels")
    public ApiResponse<?> getLevels(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.level)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/forms")
    public ApiResponse<?> getCards(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.formType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/disease-place")
    public ApiResponse<?> getDeceasePlace(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diseasePlace)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/category")
    public ApiResponse<?> getCategory(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.category)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/profession")
    public ApiResponse<?> getProfession(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findFirst20ByTypeAndSearch(CatalogType.profession, search)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/defined-disease")
    public ApiResponse<?> definedDisease(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.definedDisease)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/neighborhood")
    public ApiResponse<?> getCatalogNeighborhood(
            @RequestParam String parentCode,
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    //Catalog 175
    @GetMapping("/where-patient-come")
    public ApiResponse<?> wherePatientCome(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.wherePatientCome)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/hospital-discharge")
    public ApiResponse<?> hospitalDischargeStatus(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.hospitalDischargeStatus)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/transport-type")
    public ApiResponse<?> transportType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.transportType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/reason-of-leaving-home")
    public ApiResponse<?> reasonOfLeavingHome(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.reasonOfLeavingHome)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/reason-of-hospitalization")
    public ApiResponse<?> reasonOfLateHospitalization(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.reasonOfLateHospitalization)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/diagnosis-confirmed")
    public ApiResponse<?> diagnosisConfirmed(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diagnosisConfirmed)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/information-about-vaccination")
    public ApiResponse<?> informationAboutVaccination(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.informationAboutVaccination)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/initial-diagnosis")
    public ApiResponse<?> initialDiagnosis(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.initialDiagnosis)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/patient-identified")
    public ApiResponse<?> patientIdentified(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.patientIdentified)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/information-about-immediate-prevention")
    public ApiResponse<?> informationAboutImmediatePreventionAndAntirabicAid(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.informationAboutImmediatePreventionAndAntirabicAid)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/part-of-injury")
    public ApiResponse<?> partOfInjury(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.partOfInjury)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/relevance-of-disease")
    public ApiResponse<?> relevanceOfDiseaseToProfession(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.relevanceOfDiseaseToProfession)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/severity-of-illness")
    public ApiResponse<?> severityOfIllness(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.severityOfIllness)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/disease-spreader-type")
    public ApiResponse<?> diseaseSpreaderType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diseaseSpreaderType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/owner-of-disease-spreader")
    public ApiResponse<?> ownerOfDiseaseSpreader(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.ownerOfDiseaseSpreader)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/observation-result-animals")
    public ApiResponse<?> observationResultOfAnimals(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.observationResultOfAnimals)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/checking-diagnosis")
    public ApiResponse<?> checkingDiagnosis(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.checkingDiagnosis)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/disease-transmission-condition")
    public ApiResponse<?> diseaseTransmissionCondition(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diseaseTransmissionCondition)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/pathogen-factor")
    public ApiResponse<?> pathogenMainFactor(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.pathogenMainFactor)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/taken-measures-from-refidence")
    public ApiResponse<?> takenMeasuresFromResidence(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.takenMeasuresFromResidence)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/delivery-method")
    public ApiResponse<?> deliveryMethod(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.transportType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/home-stay-reason")
    public ApiResponse<?> homeStayExclusionReason(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.reasonOfLeavingHome)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/late-admission-reason")
    public ApiResponse<?> lateAdmissionReason(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.reasonOfLateHospitalization)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());

    }

    @GetMapping("/diagnosis-verified")
    public ApiResponse<?> diagnosisVerified(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diagnosisVerified)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/animal-category")
    public ApiResponse<?> animalCategory(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.animalCategory)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/bitten-animal-category")
    public ApiResponse<?> bittenAnimalCategory(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.animalCategory)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/dog-owner-compliance")
    public ApiResponse<?> dogOwnerCompliance(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.dogOwnerCompliance)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/condition-of-animal")
    public ApiResponse<?> conditionOfAnimal(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.conditionOfAnimal)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/animal-conservation")
    public ApiResponse<?> animalConservation(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.animalConservation)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/affected-animal-type")
    public ApiResponse<?> affectedAnimalType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.affectedAnimalType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/animal-ownership")
    public ApiResponse<?> animalOwnership(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.ownerOfDiseaseSpreader)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/animal-disposal-method")
    public ApiResponse<?> animalDisposalMethod(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.animalDisposalMethod)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/deratization")
    public ApiResponse<?> deratization(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.deratization)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/quarantine-type")
    public ApiResponse<?> quarantineType(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.quarantineType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/disinfection-transmission-factors")
    public ApiResponse<?> disinfectionTransmissionFactors(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.disinfectionTransmissionFactors)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("disease_factors")
    public ApiResponse<?> diseaseFactors(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diseaseFactors)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("processing-method")
    public ApiResponse<?> processingMethod(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.processingMethod)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/elimination-method")
    public ApiResponse<?> eliminationMethod(
            @RequestHeader(ACCEPT_LANGUAGE) String lang
    ) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.eliminationMethod)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/risk-factor")
    public ApiResponse<?> getRiskFactor(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.riskFactor)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/patient-detected")
    public ApiResponse<?> getDetected(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.detected)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

//
//    @GetMapping("/vaccination-verified")
//    public ApiResponse<?> getHoeStayReason(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
//        return catalogService.findAllByType(CatalogType.vaccinationVerified, lang);
//    }


    @GetMapping("/living-condition")
    public ApiResponse<?> getLivingCon(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.livingCondition)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/water-supply")
    public ApiResponse<?> getWaterSupply(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.waterSupply)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/liq-waste-type")
    public ApiResponse<?> getLqwT(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.liquidWaste)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/solid-waste-type")
    public ApiResponse<?> getSolidWaste(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.solidWaste)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/room-condition")
    public ApiResponse<?> getRoomCon(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.roomCondition)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/yard-condition")
    public ApiResponse<?> getYardCon(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.yardCondition)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/area-condition")
    public ApiResponse<?> getAreaCon(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.areaContidion)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/visit-objects")
    public ApiResponse<?> getVisitObj(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.visitetObj)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/prevent-measure")
    public ApiResponse<?> getPreventMeasure(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.preventMeasure)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/conducted-location")
    public ApiResponse<?> getConductedLoc(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.counductedLoca)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/infection-location")
    public ApiResponse<?> getInfectionLoc(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.infectedLocation)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/probable-infection-location")
    public ApiResponse<?> getProbableInfectionLoc(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.probableInfectionLoc)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/infection-source")
    public ApiResponse<?> getInfectionSource(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.notFoundInfection)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/infection-period")
    public ApiResponse<?> getInfectionPeriod(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.infectionPeriod)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/animal-type")
    public ApiResponse<?> getAnimalType(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.animalType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/main-probable-infection-factor")
    public ApiResponse<?> getMainFactor(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.mainFactor)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/infection-condition")
    public ApiResponse<?> getInfectionCond(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.infeCondition)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }


    @GetMapping("/outbreak-infection")
    public ApiResponse<?> getOutBreak(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.outbreakInfection)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/case-status")
    public ApiResponse<?> getCase(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.caseStatus)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/hierarchical")
    public PagedResponse<?> getHierarchy(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam CatalogType type,
            HttpServletRequest request
    ) {
        Page<Catalog> catalogPage = catalogService.getByHierarchical(page, size, type);
        return PagedResponse.build(page, catalogPage.map(catalogMapper::toDto), getRequestUrl(request), messageUtil);
    }

    @GetMapping("/disease-cause")
    public ApiResponse<?> getDiseaseCause(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.diseaseCause)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/organization-type")
    public ApiResponse<?> getOrganizationType(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.organizationType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @GetMapping("/affiliation-types")
    public ApiResponse<?> getAffiliationTypes(@RequestHeader(ACCEPT_LANGUAGE) String lang) {
        return ApiResponse.success(messageUtil, catalogService.findAllByType(CatalogType.affiliationType)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }
}
