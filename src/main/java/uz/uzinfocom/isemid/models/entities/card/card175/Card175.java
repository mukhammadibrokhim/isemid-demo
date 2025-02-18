package uz.uzinfocom.isemid.models.entities.card.card175;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "where_patient_come_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog wherePatientCome; //Bemor qayerdan kelgan.

    private String patientCome; //Bemor qayerdan kelgan.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hospital_discharge_status_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog hospitalDischargeStatus; //Kasalxonadan chiqish xolati.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transport_type_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog transportType; //Yotkizilgan kasalxona, qaysi transport vositasi bilan.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reason_of_leaving_home_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog reasonOfLeavingHome; //Uyda qoldirish (sababi) yo‘qligi.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reason_of_late_hospitalization_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog reasonOfLateHospitalization; //Kech yotqizilish sabablari.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "is_diagnosis_confirmed_code", referencedColumnName = "code")
    private Catalog diagnosisConfirmed; //Tashxis tasdiqlandi. Laboratoriya tekshiruvi  utkazildi.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "information_about_vaccination_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog informationAboutVaccination; //Vaksinatsiya o‘tkazilishi ma’lumotlar.

    private String informationAboutLastVaccination; //Oxirgi emlash haqida ma’lumot (rejali, epidemik ko‘rsatma bo‘yicha, vaqti, dozasi preparat nomi, seriyasi.

    private String briefEpidemiologicalComment; //Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "initial_diagnosis_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog initialDiagnosis; //Birlamchi tashxis.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_identified_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    // need to add field dropdown select from fields of LIS(Qo‘shimcha oyna: JSHSHIR orqali qidiruv LIS dan tanlangan javoblar tortiladi.)
    private Catalog patientIdentified; //Bemor aniqlandi.

    private String placeOfApplication; //Murojaat etgan joy.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "information_about_immediate_prevention_and_antirabic_aid_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog informationAboutImmediatePreventionAndAntirabicAid; //Tezkor profilaktika yoki antirabik yordam haqida ma’lumot.(jadval buzilganda yoki emlash o‘tkazilmagan holda).

    private String nameOfMedicine; //Dori nomi.

    private Long quantityOfMedicine; //Miqdori.

    private Long serialNumber; //Seriyasi.

    private Long vaccinationCount; //O‘tkazish jadvali.

    private String clinicalForm; //Klinik shakli.

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card175_part_of_injury",
            joinColumns = @JoinColumn(name = "card175_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> partOfInjury= new ArrayList<>();//Jarohatlangan joyi. (dropdown multiselect)

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "severity_of_illness_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog severityOfIllness; //Kasallik kechishining og‘irligi.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "relevance_of_disease_to_profession_code", referencedColumnName = "code")
    private Catalog relevanceOfDiseaseToProfession; //Kasallik kasbiy tegishliligi.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "disease_spreader_type_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog diseaseSpreaderType; // Kasallik manbai haqida ma’lumot (kuchuklar,qora mollar,itlar)

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_of_disease_spreader_code", referencedColumnName = "code")
    private Catalog ownerOfDiseaseSpreader; //Hayvonlarni kimga tegishliligi.\

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "observation_result_of_animals_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog observationResultOfAnimals; //Xayvonlarning kuzatish natijasi.

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "is_checking_diagnosis_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog checkingDiagnosis; //Hayvondagi tashxis laboratoriyada tekshiruuvda.(Tasdiqlandi,Tasdiqlanmagan,Tekshirilmagan)

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card175_disease_transmission_condition",
            joinColumns = @JoinColumn(name = "card175_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> diseaseTransmissionCondition; //Kasallik yuqishiga olib keladigan sharoit. (dropdown multiselect)

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card175_pathogen_main_factor",
            joinColumns = @JoinColumn(name = "card175_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> pathogenMainFactor; //Kasallik chaqiruvchisini yuqtirishga ehtimol qilingan asosiy omil.(dropdown multiselect)

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card175_taken_measures_from_residence",
            joinColumns = @JoinColumn(name = "card175_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> takenMeasuresFromResidence; //Bemorning yashash joyida o‘tkazilgan chora - tadbirlar.(dropdown multiselect)


}