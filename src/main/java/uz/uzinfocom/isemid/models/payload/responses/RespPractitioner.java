package uz.uzinfocom.isemid.models.payload.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RespPractitioner {
    private String id;
    private Meta meta;
    private Name name;
    private boolean active;
    private String gender;
    private List<Address> address;
    private List<Telecom> telecom;
    private LocalDate birthDate;
    private List<Identifier> identifier;
    private String resourceType;

    @Getter
    @Setter
    public static class Meta {
        private String versionId;
        private String lastUpdated;
    }

    @Getter
    @Setter
    public static class Name {
        private String lastName;
        private String firstName;
        private String patronymic;
    }

    @Getter
    @Setter
    public static class Address {
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
    public static class Telecom {
        private String use;
        private String value;
        private String system;
    }

    @Getter
    @Setter
    public static class Identifier {
        private String use;
        private Type type;
        private String value;

        @Getter
        @Setter
        public static class Type {
            private List<Coding> coding;

            @Getter
            @Setter
            public static class Coding {
                private String code;
                private String system;
                private String display;
            }
        }
    }
}
