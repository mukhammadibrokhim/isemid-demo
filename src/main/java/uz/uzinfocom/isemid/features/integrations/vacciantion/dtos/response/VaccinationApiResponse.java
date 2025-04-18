package uz.uzinfocom.isemid.features.integrations.vacciantion.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VaccinationApiResponse {
    private List<Data> result;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String id;
        private boolean allowed;
        private boolean acceptedMeasure;
        private int status;
        private int type;
        private int reaction;
        private OffsetDateTime vaccinationDate;
        private int vaccinationPeriod;
        private String pin;
        private String firstName;
        private String lastName;
        private String middleName;
        private Date birthDate;
        private int gender;
        private String phoneNumber;
        private String email;
        private String address;
        private String mpi;
        private String vaccinatedMpi;
        private String note;
        private String complication;
        private Date complicationDate;
        private Date appealDate;
        private SkuDto sku;
        private String guardian;
        private String guardianPhoneNumber;
        private String medicineName;
        private String diagnoseName;
        private String contingentName;
        private String injectionName;
        private String vaccinationExpires;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SkuDto {
            private String id;
            private String name;
            private String nameEn;
            private String nameUz;
            private String nameKa;
            private String serialNumber;
            private Date expirationDate;
            private int defaultSkuType;
            private int wasteLevel;
            private int doseVolume;
            private int unitInMl;
            private int unitInNumber;
            private int unitInDose;
            private int doseInBottle;
            private int bottleInBox;
            private int boxKit;
            private double priceUzs;
            private double priceUsd;
            private String medicineName;
            private String manufacturerName;
            private String countryName;
        }

    }
}
