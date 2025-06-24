package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;


@RestController
@RequestMapping(ApiPaths.Catalog.ROOT)
@Tag(name = "Catalog Look UP")
@RequiredArgsConstructor
public class CatalogLookupController {
    private final CatalogService catalogService;

    @Operation(
            summary = "Получить все типы каталогов",
            description = "Получает список всех типов каталогов, доступных в системе.")
    @GetMapping("/types")
    public ApiResponse<?> getCatalogs(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.catalogType, lang);
    }

    @Operation(
            summary = "Получить все состояния",
            description = "Получает список всех доступных состояний из каталога.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/state")
    public ApiResponse<?> getCatalogState(@RequestHeader("Accept-Language") String lang) {
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
    public ApiResponse<?> getCatalogCity(
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
    public ApiResponse<?> getIdentifierType(
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
    public ApiResponse<?> getGender(
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
    public ApiResponse<?> getCountry(
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
    public ApiResponse<?> getNationality(
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
    public ApiResponse<?> getMaritalStatus(
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
    public ApiResponse<?> getResidentialStatus(
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
    public ApiResponse<?> getAddressType(
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
    public ApiResponse<?> getPopulationType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.populationType, lang);
    }

    @GetMapping("/positions")
    public ApiResponse<?> getPositions(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.position, lang);
    }

    @GetMapping("/levels")
    public ApiResponse<?> getLevels(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.level, lang);
    }

    @GetMapping("/forms")
    public ApiResponse<?> getCards(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.formType, lang);
    }

    @GetMapping("/disease-place")
    public ApiResponse<?> getDeceasePlace(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.diseasePlace, lang);
    }

    @GetMapping("/category")
    public ApiResponse<?> getCategory(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.category, lang);
    }

    @GetMapping("/profession")
    public ApiResponse<?> getProfession(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.profession, lang, search);
    }

    @GetMapping("/defined-disease")
    public ApiResponse<?> definedDisease(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.definedDisease, lang);
    }

    @GetMapping("/neighborhood")
    public ApiResponse<?> getCatalogNeighborhood(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode, lang);

    }

    //Catalog 175
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

    
    @GetMapping("/risk-factor")
    public ApiResponse<?> getRiskFactor(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.riskFactor, lang);
    }

    
    @GetMapping("/patient-detected")
    public ApiResponse<?> getDetected(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.detected, lang);
    }

//    
//    @GetMapping("/vaccination-verified")
//    public ApiResponse<?> getHoeStayReason(@RequestHeader("Accept-Language") String lang) {
//        return catalogService.findAllByType(CatalogType.vaccinationVerified, lang);
//    }

    
    @GetMapping("/living-condition")
    public ApiResponse<?> getLivingCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.livingCondition, lang);
    }

    
    @GetMapping("/water-supply")
    public ApiResponse<?> getWaterSupply(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.waterSupply, lang);
    }

    
    @GetMapping("/liq-waste-type")
    public ApiResponse<?> getLqwT(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.liquidWaste, lang);
    }

    
    @GetMapping("/solid-waste-type")
    public ApiResponse<?> getSolidWaste(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.solidWaste, lang);
    }

    
    @GetMapping("/room-condition")
    public ApiResponse<?> getRoomCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.roomCondition, lang);
    }

    
    @GetMapping("/yard-condition")
    public ApiResponse<?> getYardCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.yardCondition, lang);
    }

    
    @GetMapping("/area-condition")
    public ApiResponse<?> getAreaCon(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.areaContidion, lang);
    }

    
    @GetMapping("/visit-objects")
    public ApiResponse<?> getVisitObj(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.visitetObj, lang);
    }

    
    @GetMapping("/prevent-measure")
    public ApiResponse<?> getPreventMeasure(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.preventMeasure, lang);
    }

    
    @GetMapping("/conducted-location")
    public ApiResponse<?> getConductedLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.counductedLoca, lang);
    }

    
    @GetMapping("/infection-location")
    public ApiResponse<?> getInfectionLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infectedLocation, lang);
    }

    
    @GetMapping("/probable-infection-location")
    public ApiResponse<?> getProbableInfectionLoc(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.probableInfectionLoc, lang);
    }

    
    @GetMapping("/infection-source")
    public ApiResponse<?> getInfectionSource(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.notFoundInfection, lang);
    }

    
    @GetMapping("/infection-period")
    public ApiResponse<?> getInfectionPeriod(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infectionPeriod, lang);
    }

    
    @GetMapping("/animal-type")
    public ApiResponse<?> getAnimalType(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.animalType, lang);
    }

    
    @GetMapping("/main-probable-infection-factor")
    public ApiResponse<?> getMainFactor(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.mainFactor, lang);
    }

    
    @GetMapping("/infection-condition")
    public ApiResponse<?> getInfectionCond(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.infeCondition, lang);
    }

    
    @GetMapping("/outbreak-infection")
    public ApiResponse<?> getOutBreak(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.outbreakInfection, lang);
    }

    
    @GetMapping("/case-status")
    public ApiResponse<?> getCase(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.caseStatus, lang);
    }

    @GetMapping("/hierarchical")
    public PagedResponse<?> getHierarchy(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam CatalogType type,
            HttpServletRequest request
    ) {
        return catalogService.getByHierarchical(page, size, type, getRequestUrl(request));
    }

    @GetMapping("/disease-cause")
    public ApiResponse<?> getDiseaseCause(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.diseaseCause, lang);
    }

    @GetMapping("/organization-type")
    public ApiResponse<?> getOrganizationType(@RequestHeader("Accept-Language") String lang) {
       return catalogService.findAllByType(CatalogType.organizationType, lang);
    }
}
