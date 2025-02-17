package uz.uzinfocom.isemid.models.payload.requests.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD175 type card")
public class ReqCard175 extends ReqCard{

    private LocalDateTime timeOfEpidemiologicalInvestigation;
    private Date dateOfIllness;
    private LocalDateTime dateOfDiagnosisOfEmergencyDay;
    private LocalDateTime dateOfFinalDiagnosis;
    private Date dateOfDischargeFromHospital;
    private Date dateOfVaccination;//Emlash sanasi
    private String pathogenType; //Qo‘zg‘atuvchi turi, seroguruxi.


    private String informationAboutLastVaccination; //Oxirgi emlash haqida ma’lumot (rejali, epidemik ko‘rsatma bo‘yicha, vaqti, dozasi preparat nomi, seriyasi.
    private String briefEpidemiologicalComment; //Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).
    private String placeOfApplication; //Murojaat etgan joy.
    private String nameOfMedicine; //Dori nomi.
    private Long quantityOfMedicine; //Miqdori.
    private Long serialNumber; //Seriyasi.
    private Long vaccinationCount; //O‘tkazish jadvali.
    private String clinicalForm; //Klinik shakli.

    //Catalogs
    private String wherePatientCome;//Bemor qayerdan kelgan.
    private String hospitalDischargeStatusCode;//Kasalxonadan chiqish xolati.
    private String transportTypeCode; //Yotkizilgan kasalxona, qaysi transport vositasi bilan.
    private String reasonOfLeavingHomeCode; //Uyda qoldirish (sababi) yo‘qligi.
    private String reasonOfLateHospitalizationCode; //Kech yotqizilish sabablari.
    private String diagnosisConfirmedCode; //Tashxis tasdiqlandi. Laboratoriya tekshiruvi  utkazildi.
    private String informationAboutVaccinationCode; //Vaksinatsiya o‘tkazilishi ma’lumotlar.
    private String initialDiagnosisCode; //Birlamchi tashxis.
    private String patientIdentifiedCode; //Bemor aniqlandi.
    private String informationAboutImmediatePreventionAndAntirabicAidCode; //Tezkor profilaktika yoki antirabik yordam haqida ma’lumot.(jadval buzilganda yoki emlash o‘tkazilmagan holda).
    private List<String> partOfInjuryCode; //Jarohatlangan joyi.
    private String severityOfIllnessCode; //Kasallik kechishining og‘irligi.
    private String relevanceOfDiseaseToProfessionCode; //Kasallik kasbiy tegishliligi.
    private String diseaseSpreaderTypeCode; // Kasallik manbai haqida ma’lumot (kuchuklar,qora mollar,itlar)
    private String ownerOfDiseaseSpreaderCode; //Hayvonlarni kimga tegishliligi.
    private String observationResultOfAnimalsCode; //Xayvonlarning kuzatish natijasi.
    private String checkingDiagnosisCode; //Hayvondagi tashxis laboratoriyada tekshiruuvda.(Tasdiqlandi,Tasdiqlanmagan,Tekshirilmagan)
    private List<String> diseaseTransmissionConditionCode; //Kasallik yuqishiga olib keladigan sharoit.
    private List<String> pathogenMainFactorCode; //Kasallik chaqiruvchisini yuqtirishga ehtimol qilingan asosiy omil.
    private List<String> takenMeasuresFromResidenceCode;//Bemorning yashash joyida o‘tkazilgan chora - tadbirlar.


}
