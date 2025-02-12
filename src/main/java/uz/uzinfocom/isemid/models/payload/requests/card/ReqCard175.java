package uz.uzinfocom.isemid.models.payload.requests.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;
import java.util.Date;

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

}
