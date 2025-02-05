package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;
import java.util.Date;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "card_175")
public class Card175 extends Card {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "CARD_175")
    @SequenceGenerator(sequenceName = "CARD_175", allocationSize = 1, name = "CARD_175")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
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

    @ManyToOne
    @JoinColumn(name = "where_patient_come_code")
    private Catalog wherePatientCome; //Bemor qayerdan kelgan.

    private String patientCome; //Bemor qayerdan kelgan.
    @ManyToOne
    @JoinColumn(name = "hospital_discharge_status_code")
    private Catalog hospitalDischargeStatus; //Kasalxonadan chiqish xolati.
    @ManyToOne
    @JoinColumn(name = "transport_type_code")
    private Catalog transportType; //Yotkizilgan kasalxona, qaysi transport vositasi bilan.
    @ManyToOne
    @JoinColumn(name = "reason_of_leaving_home_code")
    private Catalog reasonOfLeavingHome; //Uyda qoldirish (sababi) yo‘qligi.
    @ManyToOne
    @JoinColumn(name = "reason_of_late_hospitalization_code")
    private Catalog reasonOfLateHospitalization; //Kech yotqizilish sabablari.
    @ManyToOne
    @JoinColumn(name = "is_diagnosis_confirmed_code")
    private Catalog isDiagnosisConfirmed; //Tashxis tasdiqlandi. Laboratoriya tekshiruvi  utkazildi.

    @ManyToOne
    @JoinColumn(name = "information_about_vaccination_code")
    private Catalog informationAboutVaccination; //Vaksinatsiya o‘tkazilishi ma’lumotlar.

    private String informationAboutLastVaccination; //Oxirgi emlash haqida ma’lumot (rejali, epidemik ko‘rsatma bo‘yicha, vaqti, dozasi preparat nomi, seriyasi.

    private String briefEpidemiologicalComment; //Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).

    @ManyToOne
    @JoinColumn(name = "initial_diagnosis_code")
    private Catalog initialDiagnosis; //Birlamchi tashxis.

    @ManyToOne
    @JoinColumn(name = "patient_identified_code")
    // need to add field dropdown select from fields of LIS(Qo‘shimcha oyna: JSHSHIR orqali qidiruv LIS dan tanlangan javoblar tortiladi.)
    private Catalog patientIdentified; //Bemor aniqlandi.

    private String placeOfApplication; //Murojaat etgan joy.

    @ManyToOne
    @JoinColumn(name = "immediate_prevention_and_antirabic_aid_code")
    private Catalog immediatePreventionAndAntirabicAid; //Tezkor profilaktika va antirabik yordam.

    @ManyToOne
    @JoinColumn(name = "information_about_immediate_prevention_and_antirabic_aid_code")
    private Catalog informationAboutImmediatePreventionAndAntirabicAid; //Tezkor profilaktika yoki antirabik yordam haqida ma’lumot.(jadval buzilganda yoki emlash o‘tkazilmagan holda).

    private String nameOfMedicine; //Dori nomi.

    private Long quantityOfMedicine; //Miqdori.

    private Long serialNumber; //Seriyasi.

    private Long vaccinationCount; //O‘tkazish jadvali.

    private String clinicalForm; //Klinik shakli.

    @ManyToOne
    @JoinColumn(name = "part_of_injury_code")
    private Catalog partOfInjury; //Jarohatlangan joyi. (dropdown multiselect)

    @ManyToOne
    @JoinColumn(name = "severity_of_illness_code")
    private Catalog severityOfIllness; //Kasallik kechishining og‘irligi.

    @ManyToOne
    @JoinColumn(name = "relevance_of_disease_to_profession_code")
    private Catalog relevanceOfDiseaseToProfession; //Kasallik kasbiy tegishliligi.

    @ManyToOne
    @JoinColumn(name = "disease_spreader_type_code")
    private Catalog diseaseSpreaderType; // Kasallik manbai haqida ma’lumot (kuchuklar,qora mollar,itlar)

    @ManyToOne
    @JoinColumn(name = "owner_of_disease_spreader_code")
    private Catalog ownerOfDiseaseSpreader; //Hayvonlarni kimga tegishliligi.\

    @ManyToOne
    @JoinColumn(name = "observation_result_of_animals_code")
    private Catalog observationResultOfAnimals; //Xayvonlarning kuzatish natijasi.

    @ManyToOne
    @JoinColumn(name = "is_checking_diagnosis_code")
    private Catalog isCheckingDiagnosis; //Hayvondagi tashxis laboratoriyada tekshiruuvda.(Tasdiqlandi,Tasdiqlanmagan,Tekshirilmagan)

    @ManyToOne
    @JoinColumn(name = "disease_transmission_condition_code")
    private Catalog diseaseTransmissionCondition; //Kasallik yuqishiga olib keladigan sharoit. (dropdown multiselect)

    @ManyToOne
    @JoinColumn(name = "pathogen_main_factor_code")
    private Catalog pathogenMainFactor; //Kasallik chaqiruvchisini yuqtirishga ehtimol qilingan asosiy omil.(dropdown multiselect)

    @ManyToOne
    @JoinColumn(name = "taken_measures_from_residence_code")
    private Catalog takenMeasuresFromResidence; //Bemorning yashash joyida o‘tkazilgan chora - tadbirlar.(dropdown multiselect)






}