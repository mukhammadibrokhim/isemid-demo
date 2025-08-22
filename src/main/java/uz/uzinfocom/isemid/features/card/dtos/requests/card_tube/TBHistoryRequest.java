package uz.uzinfocom.isemid.features.card.dtos.requests.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TBHistoryRequest {
    private Long id;
    private String location; // Infeksiya joyi (organ yoki tana qismi)
    private LocalDate date; // Infeksiya aniqlangan sana
    private String mkbCode; // MKB-10 kodi
    private String mkbName; // MKB-10 nomi
    private String regGroup; // Ro‘yxatga olish guruhi
}
