package uz.uzinfocom.isemid.features.card.models.card205;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card205 extends Card {
    private String mkbCode; // MKB-10 kodi
    private String mkbName; // Jarohat joyi MKB-10 dan
    private LocalDate epiSurveyDate; // Epidemiologik surishtiruv sanasi
    private LocalDate vetAlertDate; // Veterinariya xizmati ogohlantirilgan sana
    private String biteAddress; // Tishlash manzili
    private LocalDate biteDate; // Tishlash sanasi
    private String firstClinicName; // Birinchi murojaat qilingan DPM nomi
    private LocalDate firstClinicDate; // Birinchi murojaat qilingan sana

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("card205")
    @Builder.Default
    private List<OtherBittenPerson> otherBittenPersons = new ArrayList<>();

    private String animalType; // Hayvon turi
    private String animalCategoryCode; // Hayvon turi
    private String animalOrigin; // Hayvon qayerdan kelgan
    private String animalArrivalDate; // Hayvon kelgan sana
    private String animalConditionCode; // Hayvon holati kodi
    private Integer animalAge;
    private String animalBreed;
    private String animalColor;
    private String animalSigns; // Individual belgilar
    private Integer vetCertNum1; // 1-vet. tekshiruv raqami

    private LocalDate vetCertDate1; // 1-vet. ma’lumotnoma sanasi

    private String animalHoldCode; // Hayvon saqlash kodi
    private String victimPositionCode; // Jabrlanuvchi holati kodi
    private Integer vetCertNum2; // 2-vet. tekshiruv raqami

    private LocalDate vetCertDate2; // 2-vet. ma’lumotnoma sanasi

    private String sampleTakenDateTime; // Tuk olingan sana/vaqt
    private String vetDept; // Hayvon ro‘yxatdan o‘tgan bo‘lim
    private String ownerComplianceCode; // Egasi qoidaga amal qilganmi kodi

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("card205")
    @Builder.Default
    private List<OtherBittenAnimal> otherBittenAnimals = new ArrayList<>();

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("card205")
    @Builder.Default
    private List<AnimalBiteVictim> animalBiteVictims = new ArrayList<>();

    private String extraInfo; // Qo‘shimcha ma’lumot
    private String animalOwnerName; // Hayvon egasining F.I.Sh.
}
