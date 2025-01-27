package uz.uzinfocom.isemid.models.payload.responses;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespOrganization {
    private String id;
    private Meta meta;
    private List<Type> type;
    private String name;
    private boolean active;
    private PartOf partOf;
    private List<Address> address;
    private List<Telecom> telecom;
    private List<ServiceArea> serviceArea;
    private String resourceType;
    private List<Identifier> identifier;

    @Getter
    @Setter
    public static class Meta {
        private String versionId;
        private String lastUpdated;
    }

    @Getter
    @Setter
    public static class OrganizationType {
        private List<Coding> coding;
    }

    @Getter
    @Setter
    public static class Coding {
        private String code;
        private String system;
        private String display;
    }

    @Getter
    @Setter
    public static class Alias {
        private String value;
        private String language;
    }

    @Getter
    @Setter
    public static class PartOf {
        private String display;
        private String reference;
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
    public static class ServiceArea {
        private String state;
    }

    @Getter
    @Setter
    public static class Identifier {
        private String use;
        private Type type;
        private String value;
        private String system;
    }

    @Getter
    @Setter
    public static class Type {
        private List<Coding> coding;
    }
}
