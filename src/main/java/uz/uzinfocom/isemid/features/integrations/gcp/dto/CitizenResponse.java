package uz.uzinfocom.isemid.features.integrations.gcp.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CitizenResponse {
    private String result;
    private List<CitizenData> data;
    private String comments;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CitizenData {
        private int transaction_id;
        private String current_pinpp;
        private List<String> pinpps;
        private String current_document;
        private List<Document> documents;
        private String surnamelat;
        private String namelat;
        private String patronymlat;
        private String surnamecyr;
        private String namecyr;
        private String patronymcyr;
        private String engsurname;
        private String engname;
        private String birth_date;
        private String birthplace;
        private String birthcountryid;
        private String livestatus;
        private int nationalityid;
        private String  citizenshipid;
        private String sex;
        private String photo;

    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Document {
        private String document;
        private String type;
        private String docgiveplace;
        private int docgiveplaceid;
        private String datebegin;
        private String dateend;
        private int status;
    }
}
