package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisinfectionInfoRequest {
    private Long id;
    private List<String> disinfectionFactors = new ArrayList<>(); // Dezinfeksiya ob'ektlari
    private Integer disinfectedCount; // Zararsizlantirilgan omillar miqdori
    private LocalDate disinfectionDate; // Zararsizlantirish sanasi
    private List<String> eliminationMethods = new ArrayList<>(); // Yo‘q qilish usullari
    private String eventLocation; // Hodisa joyi
    private String controlResults; // Chora nazorati natijasi
}
