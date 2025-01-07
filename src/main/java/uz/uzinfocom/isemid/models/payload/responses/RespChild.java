package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespChild {

    private List<Items> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Items {
        private String pnfl;
        private String surname;
        private String name;
        private String patronym;
        private String birth_date; // Format: dd.MM.yyyy
        private int gender_code;
        private String doc_num;
        private String doc_date; // Format: dd.MM.yyyy
        private int branch;
        private String cert_series;
        private String cert_number;
        private String cert_birth_date; // Format: dd.MM.yyyy
        private String f_family;
        private String f_first_name;
        private String f_patronym;
        private String f_birth_day; // Format: dd.MM.yyyy
        private String f_pnfl;
        private String m_family;
        private String m_first_name;
        private String m_patronym;
        private String m_birth_day; // Format: dd.MM.yyyy
        private String m_pnfl;
    }
}
