package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespVaccination {
    private String vaccinationName;   // Preparat nomi
    private String serialNumber;      // Seriya raqami
    private LocalDateTime vaccinationDate; // Vaqti (GMT+5 Toshkent)
    private int unitInDose;           // Birlik doza
    private int doseVolume;           // Doza hajmi

    private String scheduled;        // Rejeli emlashmi?
    private String epidemicBased;    // Epidemik ko‘rsatma asosida emlashmi?
}
