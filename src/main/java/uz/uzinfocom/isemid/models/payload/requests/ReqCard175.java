package uz.uzinfocom.isemid.models.payload.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqCard175{

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeOfEpidemiologicalInvestigation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dateOfIllness;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateOfDiagnosisOfEmergencyDay;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateOfFinalDiagnosis;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dateOfDischargeFromHospital;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dateOfVaccination;

    private String pathogenType; //Qo‘zg‘atuvchi turi, seroguruxi.
    private Catalog wherePatientCome;//Bemor qayerdan kelgan.
    private Catalog hospitalDischargeStatus;//Kasalxonadan chiqish xolati.
    private Catalog transportType; //Yotkizilgan kasalxona, qaysi transport vositasi bilan.
    private Catalog reasonOfLeavingHome; //Uyda qoldirish (sababi) yo‘qligi.
    private Catalog reasonOfLateHospitalization; //Kech yotqizilish sabablari.
    private Catalog isDiagnosisConfirmed; //Tashxis tasdiqlandi. Laboratoriya tekshiruvi  utkazildi.
    private Catalog informationAboutVaccination; //Vaksinatsiya o‘tkazilishi ma’lumotlar.
    private String informationAboutLastVaccination; //Oxirgi emlash haqida ma’lumot (rejali, epidemik ko‘rsatma bo‘yicha, vaqti, dozasi preparat nomi, seriyasi.
    private String briefEpidemiologicalComment; //Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).
    private Catalog initialDiagnosis; //Birlamchi tashxis.
    private Catalog patientIdentified; //Bemor aniqlandi.
    private String placeOfApplication; //Murojaat etgan joy.
    private Catalog immediatePreventionAndAntirabicAid; //Tezkor profilaktika va antirabik yordam.
    private Catalog informationAboutImmediatePreventionAndAntirabicAid; //Tezkor profilaktika yoki antirabik yordam haqida ma’lumot.(jadval buzilganda yoki emlash o‘tkazilmagan holda).
    private String nameOfMedicine; //Dori nomi.
    private Long quantityOfMedicine; //Miqdori.
    private Long serialNumber; //Seriyasi.
    private Long vaccinationCount; //O‘tkazish jadvali.
    private String clinicalForm; //Klinik shakli.
    private Catalog partOfInjury; //Jarohatlangan joyi.
    private Catalog severityOfIllness; //Kasallik kechishining og‘irligi.
    private Catalog relevanceOfDiseaseToProfession; //Kasallik kasbiy tegishliligi.
    private Catalog diseaseSpreaderType; // Kasallik manbai haqida ma’lumot (kuchuklar,qora mollar,itlar)
    private Catalog ownerOfDiseaseSpreader; //Hayvonlarni kimga tegishliligi.
    private Catalog observationResultOfAnimals; //Xayvonlarning kuzatish natijasi.
    private Catalog isCheckingDiagnosis; //Hayvondagi tashxis laboratoriyada tekshiruuvda.(Tasdiqlandi,Tasdiqlanmagan,Tekshirilmagan)
    private Catalog diseaseTransmissionCondition; //Kasallik yuqishiga olib keladigan sharoit.
    private Catalog pathogenMainFactor; //Kasallik chaqiruvchisini yuqtirishga ehtimol qilingan asosiy omil.
    private Catalog takenMeasuresFromResidence;//Bemorning yashash joyida o‘tkazilgan chora - tadbirlar.


    public ReqCard175(Card175 card175){
        setTimeOfEpidemiologicalInvestigation(LocalDateTime.now());
        setDateOfIllness(card175.getDateOfIllness());
        setDateOfDiagnosisOfEmergencyDay(LocalDateTime.now());
        setDateOfFinalDiagnosis(LocalDateTime.now());
        setDateOfDischargeFromHospital(card175.getDateOfDischargeFromHospital());
        setDateOfVaccination(card175.getDateOfVaccination());
        setPathogenType(card175.getPathogenType());
        setWherePatientCome(card175.getWherePatientCome());
        setHospitalDischargeStatus(card175.getHospitalDischargeStatus());
        setTransportType(card175.getTransportType());
        setReasonOfLeavingHome(card175.getReasonOfLeavingHome());
        setReasonOfLateHospitalization(card175.getReasonOfLateHospitalization());
        setIsDiagnosisConfirmed(card175.getIsDiagnosisConfirmed());
        setInformationAboutVaccination(card175.getInformationAboutVaccination());
        setInformationAboutLastVaccination(card175.getInformationAboutLastVaccination());
        setBriefEpidemiologicalComment(card175.getBriefEpidemiologicalComment());
        setInitialDiagnosis(card175.getInitialDiagnosis());
        setPatientIdentified(card175.getPatientIdentified());
        setPlaceOfApplication(card175.getPlaceOfApplication());
        setImmediatePreventionAndAntirabicAid(card175.getImmediatePreventionAndAntirabicAid());
        setInformationAboutImmediatePreventionAndAntirabicAid(card175.getInformationAboutImmediatePreventionAndAntirabicAid());
        setNameOfMedicine(card175.getNameOfMedicine());
        setQuantityOfMedicine(card175.getQuantityOfMedicine());
        setSerialNumber(card175.getSerialNumber());
        setVaccinationCount(card175.getVaccinationCount());
        setClinicalForm(card175.getClinicalForm());
        setPartOfInjury(card175.getPartOfInjury());
        setSeverityOfIllness(card175.getSeverityOfIllness());
        setRelevanceOfDiseaseToProfession(card175.getRelevanceOfDiseaseToProfession());
        setDiseaseSpreaderType(card175.getDiseaseSpreaderType());
        setOwnerOfDiseaseSpreader(card175.getOwnerOfDiseaseSpreader());
        setObservationResultOfAnimals(card175.getObservationResultOfAnimals());
        setIsCheckingDiagnosis(card175.getIsCheckingDiagnosis());
        setDiseaseTransmissionCondition(card175.getDiseaseTransmissionCondition());
        setPathogenMainFactor(card175.getPathogenMainFactor());
        setTakenMeasuresFromResidence(card175.getTakenMeasuresFromResidence());

    }
}
