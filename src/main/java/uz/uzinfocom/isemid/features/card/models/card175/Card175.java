package uz.uzinfocom.isemid.features.card.models.card175;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card175 extends Card {

    private LocalDateTime timeOfEpidemiologicalInvestigation; //Epidimiologik tekshiruv o‘tkazilgan vaqt

    @Temporal(value = TemporalType.DATE)
    private Date dateOfIllness; //Kasallangan sana

    private LocalDateTime dateOfDiagnosisOfEmergencyDay; //Shoshilinch habarnoma berilgan kungi tashxisni aniqlash sanasi.

    private LocalDateTime dateOfFinalDiagnosis; //Yakuniy diagnoz qo‘yilgan sana.

    @Temporal(value = TemporalType.DATE)
    private Date dateOfDischargeFromHospital; //Kasalxonadan chiqish sanasi.

    @Temporal(value = TemporalType.DATE)
    private Date dateOfVaccination; //Emlash sanasi.
    private String pathogenType; //Qo‘zg‘atuvchi turi, seroguruxi.
    private String wherePatientComeCode; //Bemor qayerdan kelgan.
    private String patientComeCode; //Bemor qayerdan kelgan.
    private String hospitalDischargeStatusCode; //Kasalxonadan chiqish xolati.
    private String transportTypeCode; //Yotkizilgan kasalxona, qaysi transport vositasi bilan.
    private String reasonOfLeavingHomeCode; //Uyda qoldirish (sababi) yo‘qligi.
    private String reasonOfLateHospitalizationCode; //Kech yotqizilish sabablari.
    private String diagnosisConfirmedCode; //Tashxis tasdiqlandi. Laboratoriya tekshiruvi  utkazildi.
    private String informationAboutVaccinationCode; //Vaksinatsiya o‘tkazilishi ma’lumotlar.
    private String informationAboutLastVaccination; //Oxirgi emlash haqida ma’lumot (rejali, epidemik ko‘rsatma bo‘yicha, vaqti, dozasi preparat nomi, seriyasi.
    private String briefEpidemiologicalComment; //Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).
    private String initialDiagnosisCode; //Birlamchi tashxis.

    // need to add field dropdown select from fields of LIS(Qo‘shimcha oyna: JSHSHIR orqali qidiruv LIS dan tanlangan javoblar tortiladi.)
    private String patientIdentifiedCode; //Bemor aniqlandi.
    private String placeOfApplication; //Murojaat etgan joy.
    private String preventionAndAidCode; //Tezkor profilaktika yoki antirabik yordam haqida ma’lumot.(jadval buzilganda yoki emlash o‘tkazilmagan holda).
    private String nameOfMedicine; //Dori nomi.
    private Long quantityOfMedicine; //Miqdori.
    private Long serialNumber; //Seriyasi.
    private Long vaccinationCount; //O‘tkazish jadvali.
    private String clinicalForm; //Klinik shakli.

    @ElementCollection
    @CollectionTable(
            name = "card175_part_of_injury",
            joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")  // Faqat `catalogCode` qiymatini saqlaymiz
    private List<String> partOfInjury = new ArrayList<>();//Jarohatlangan joyi. (dropdown multiselect)

    private String severityOfIllnessCode; //Kasallik kechishining og‘irligi.
    private String relevanceOfDiseaseToProfessionCode; //Kasallik kasbiy tegishliligi.
    private String diseaseSpreaderTypeCode; // Kasallik manbai haqida ma’lumot (kuchuklar,qora mollar,itlar)
    private String ownerOfDiseaseSpreaderCode; //Hayvonlarni kimga tegishliligi.\
    private String observationResultOfAnimalsCode; //Xayvonlarning kuzatish natijasi.
    private String checkingDiagnosisCode; //Hayvondagi tashxis laboratoriyada tekshiruuvda.(Tasdiqlandi,Tasdiqlanmagan,Tekshirilmagan)

    @ElementCollection
    @CollectionTable(
            name = "card175_disease_transmission_condition",
            joinColumns = @JoinColumn(name = "card175_id")
    )
    @Column(name = "catalog_code")  // Faol `catalogCode` qiymatini saqlash
    private List<String> diseaseTransmissionConditionCode = new ArrayList<>();//Kasallik yuqishiga olib keladigan sharoit. (dropdown multiselect)

    @ElementCollection
    @CollectionTable(
            name = "card175_pathogen_main_factor",
            joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> pathogenMainFactor = new ArrayList<>();//Kasallik chaqiruvchisini yuqtirishga ehtimol qilingan asosiy omil.(dropdown multiselect)

    @ElementCollection
    @CollectionTable(
            name = "card175_taken_measures_from_residence",
            joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> takenMeasuresFromResidence = new ArrayList<>();//Bemorning yashash joyida o‘tkazilgan chora - tadbirlar.(dropdown multiselect)
}