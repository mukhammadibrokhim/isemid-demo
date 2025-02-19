package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespVaccination {
    private String firstName;
    private String lastName;
    private String middleName;
    private Date birthDate;
    private String mpi;
    private String vaccinationName;   // Preparat nomi
    private String serialNumber;      // Seriya raqami
    private LocalDateTime vaccinationDate; // Vaqti (GMT+5 Toshkent)
    private int unitInDose;           // Birlik doza
    private int doseVolume;           // Doza hajmi
    private String vaccinationExpire;
}
