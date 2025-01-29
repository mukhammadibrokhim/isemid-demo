package uz.uzinfocom.isemid.models.entities.card;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card174 extends Card {
    private String pathogenType; // Qo‘zg‘atuvchisining turi (serogruppa)
    private LocalDate dataObtainedDate; // Hayvonlar kasalligi yoki kasallikka shubha tug‘ilganda olingan ma’lumot sanasi
    private LocalDate reportToSanitaryCommitteeDate; // Tuman Sanitariya Epidemiologiya Qo‘mitasiga ma’lumot berilagn sana
    private LocalDate reportToVeterinaryDepartmentDate; // Tuman Vetenariya bo‘limiga ma’lumot berilgan sana
    private String sentBy; // Kim tomonidan yuborilgan, muassasa, lavozimi
    private LocalDate humanInfectionDataDate; // Odamni kasallanganligi to‘g‘risida olingan ma’lumot sanasi
    private String animalPrimaryDiagnosis; // Hayvondagi birlamchi tashxisi (dastlabki)
    private String humanPrimaryDiagnosis; // Odamdagi birlamchi tashxisi (dastlabki)
    private LocalDate investigationDate; // Epizootologik-epidemiologik tekshiruv o‘tkazilgan sana
    private int lastDiseaseYear; // Hayvonlar va odamlar orasida kasallik qayd etilgan so‘ngi yil
    private LocalDate currentAnimalInfectionDate; // Hayvon(lar)ning hozirgi kasallangan sanasi
    private String outbreakLocalization; // O‘chog‘ni lokalizatsiyasi (xo‘jalik, aholi punkti, korxona sex)
    private String animalOwner; // Hayvon egasi, Familiya, Ismi Sharifi
    private String ownerAddress; // Manzilgohi
    private String affectedAnimalType; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning turi
    private int affectedAnimalCount; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning soni
    private String animalOwnership; // Hayvonlarni kimga tegishliligi
    private boolean isAreaExotic; // Ushbu hudud kasallik bo‘yicha ekzotik
    private boolean rodentIncrease; // Kemiruvchilar sonining ortishi
    private boolean vectorIncrease; // Tashuvchilar soni ortishi
    private boolean wildRodentsIncrease; // Kemiruvchilar soni ortdi (yovvoyi)
    private boolean synanthropicRodentsIncrease; // Kemiruvchilar soni ortdi (sinantrop)
    private boolean bloodSuckingArthropodsIncrease; // Bo‘g‘im oyoqli qon so‘ruvchilar soni ortdi
    private boolean epizootologyExistence; // Epizootologiya bor yo‘qligi
    private String diseaseFactors; // Bu kasallikning kelib chiqishiga sabab bo‘lgan faktorlar
    private String animalType; // Hayvon turi, tekshirilib o'tkazilgan namunalar turi
    private LocalDate testDate; // Tekshiruv sanasi
    private int testSampleCount; // Tekshirilgan namunalar soni
    private String testingMethod; // Tekshiruv usuli
    private String testResult; // Natija (serovar turi, qo‘zg‘atuvchi, titr)
    private int affectedAnimals; // Kasallangan hayvonlar soni
    private int affectedHumans; // Kasallangan odamlar soni
    private int treatedHumans; // Tezkor yordam yoki antirabik yordam olganlar soni
    private int affectedInOutbreak; // O‘choqda kasallangan odamlar soni
    private String executorName; // Chora-tadbirlarni amalga oshiruvchi shaxs ismi
}
