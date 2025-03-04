package uz.uzinfocom.isemid.models.payload.requests.card.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqInfectionMonitoring {
    private Long id;
    private Integer sequentialNumber;
    private String lastName;
    private String firstName;
    private String middleName;
    private String genderCode;
    private LocalDate birthDate;
    private String address;//Manzil.
    private String profession;//Kasbi, ish joyi.
    private LocalDate applicationDate;//Murojat sanasi.
    private LocalDate confirmationDate;//Aniqlangan sana.
    private String possibleInfectionLocation;//Yuqtirilishi mumkin bo‘lgan joyi.
    private String possibleInfectionFactor;//Yuqtirilishi mumkin bo‘lgan yuqish omili.
    private LocalDate possibleInfectionDate;//Yuqtirilishi mumkin bo‘lgan sana.
}
