package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_contact_monitoring")
public class ContactMonitoring extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube; // Bu kuzatuv yozuvi qaysi CardTube kartasiga tegishli

    private String fullName; // F.I.Sh. (To‘liq ism sharifi)
    private LocalDate birthDate; // Tug‘ilgan sana (Date of Birth)
    private Integer age; // Yoshi
    private String relationCode; // Qarindoshlik darajasi (masalan: ota, ona, aka)
    private String organization; // Ish yoki o‘qish joyi (Organization)
    private String notifier; // Xabar olgan shaxs yoki muassasa
    private LocalDate diagDate; // Tashxis qo‘yilgan sana (Diagnosis Date)
    private String statusCode; // Aloqa holati (Active, Monitored, Infected va h.k.)
}
