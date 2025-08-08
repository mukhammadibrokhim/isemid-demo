package uz.uzinfocom.isemid.features.card.dtos.requests.card205;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.models.card205.AnimalBiteVictim;
import uz.uzinfocom.isemid.features.card.models.card205.OtherBittenAnimal;
import uz.uzinfocom.isemid.features.card.models.card205.OtherBittenPerson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD205 type card")
public class Card205Request extends CardRequest {
    private String mkbCode; // MKB-10 kodi
    private String mkbName; // Jarohat joyi MKB-10 dan

    @Temporal(TemporalType.DATE)
    private Date epiSurveyDate; // Epidemiologik surishtiruv sanasi

    @Temporal(TemporalType.DATE)
    private Date vetAlertDate; // Veterinariya xizmati ogohlantirilgan sana

    private String biteAddress; // Tishlash manzili

    @Temporal(TemporalType.DATE)
    private Date biteDate; // Tishlash sanasi

    private String firstClinicName; // Birinchi murojaat qilingan DPM nomi

    @Temporal(TemporalType.DATE)
    private Date firstClinicDate; // Birinchi murojaat qilingan sana

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("card205")
    @Builder.Default
    private List<OtherBittenPerson> otherBittenPersons = new ArrayList<>();

    private String animalType; // Hayvon turi
    private String animalOrigin; // Hayvon qayerdan kelgan
    private String animalArrivalDate; // Hayvon kelgan sana
    private String animalConditionCode; // Hayvon holati kodi
    private Integer animalAge;
    private String animalBreed;
    private String animalColor;
    private String animalSigns; // Individual belgilar
    private Integer vetCertNum1; // 1-vet. tekshiruv raqami

    @Temporal(TemporalType.DATE)
    private Date vetCertDate1; // 1-vet. ma’lumotnoma sanasi

    private String animalHoldCode; // Hayvon saqlash kodi
    private String victimPositionCode; // Jabrlanuvchi holati kodi
    private Integer vetCertNum2; // 2-vet. tekshiruv raqami

    @Temporal(TemporalType.DATE)
    private Date vetCertDate2; // 2-vet. ma’lumotnoma sanasi

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
