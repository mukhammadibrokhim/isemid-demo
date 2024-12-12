package com.abdulatif.emis.dto.sso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PractitionerDTO {

    private String id;
    private MetaDto meta;
    private NameDto name;
    private boolean active;
    private String gender;
    private List<AddressDto> address;
    private List<TelecomDto> telecom;
    private String birthDate;
    private List<IdentifierDto> identifier;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaDto {
        private String source;
        private String versionId;
        private String lastUpdated;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameDto {
        private String lastName;
        private String firstName;
        private String patronymic;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressDto {
        private String use;
        private String city;
        private String line;
        private String type;
        private String state;
        private String country;
        private String district;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelecomDto {
        private String use;
        private int rank;
        private String value;
        private String system;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdentifierDto {
        private TypeDto type;
        private String value;
        private String system;

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TypeDto {
            private String text;
            private List<CodingDto> coding;

            @Getter
            @Setter
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class CodingDto {
                private String code;
                private String system;
                private String display;
            }
        }
    }
}