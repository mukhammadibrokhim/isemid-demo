package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisinfectionInfoResponse {
    private Long id;
    private List<String> disinfectionFactors = new ArrayList<>(); // Dezinfeksiya ob'ektlari
    private Integer disinfectedCount; // Zararsizlantirilgan omillar miqdori
    private LocalDate disinfectionDate; // Zararsizlantirish sanasi
    private List<String> eliminationMethods = new ArrayList<>(); // Yo‘q qilish usullari
    private String eventLocation; // Hodisa joyi
    private String controlResults; // Chora nazorati natijasi
}
