package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.OrganizationIdentifier;
import uz.uzinfocom.app.modules.iam.domain.OrganizationTypeCoding;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.payload.RemoteOrganizationPayload;

import static org.assertj.core.api.Assertions.assertThat;

/** Real DHP Organization shape: no alias[], translations in _name.extension, three identifiers. */
class OrganizationRemoteMapperDhpPayloadTest {

    private static final String DHP_ORGANIZATION = """
            {
              "_name": {"extension": [
                {"url": "http://hl7.org/fhir/StructureDefinition/translation", "extension": [
                  {"url": "lang", "valueCode": "ru"},
                  {"url": "content", "valueString": "ООО \\"Единый интегратор-uzinfocom\\""}]},
                {"url": "http://hl7.org/fhir/StructureDefinition/translation", "extension": [
                  {"url": "lang", "valueCode": "en"},
                  {"url": "content", "valueString": "Unified Integrator-uzinfocom LLC"}]}
              ]},
              "active": true,
              "id": "0ea5318d-5223-4a58-b02f-adfdbffeccf2",
              "identifier": [
                {"system": "https://dhp.uz/fhir/core/sid/org/uz/soliq", "use": "official",
                 "type": {"coding": [{"code": "TAX", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                 "value": "204118319"},
                {"system": "https://dhp.uz/fhir/core/sid/org/uz/argos", "use": "official",
                 "type": {"coding": [{"code": "XX", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                 "value": "859"},
                {"system": "https://dhp.uz/fhir/core/sid/org/uz/dhp", "use": "official",
                 "type": {"coding": [{"code": "XX", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                 "value": "HOR-G5H3KY4-S"}
              ],
              "name": "Yagona Integrator-uzinfocom Mchj",
              "resourceType": "Organization",
              "type": [{"coding": [
                {"code": "I_1", "system": "https://terminology.dhp.uz/fhir/core/CodeSystem/organizational-subordination-group-cs"},
                {"code": "999", "system": "https://terminology.dhp.uz/fhir/core/CodeSystem/organizational-structure-cs"}
              ]}]
            }
            """;

    private static final String DHP_DISTRICT_POLYCLINIC = """
            {
              "id": "11111111-2222-3333-4444-555555555555",
              "name": "Shahar oilaviy poliklinikasi",
              "extension": [{
                "url": "https://dhp.uz/fhir/core/StructureDefinition/coverage-area",
                "valueCodeableConcept": {"coding": [{"code": "1703", "system": "x"}]}
              }],
              "type": [{"coding": [
                {"code": "I_3", "system": "https://terminology.dhp.uz/fhir/core/CodeSystem/organizational-subordination-group-cs"},
                {"code": "351", "system": "https://terminology.dhp.uz/fhir/core/CodeSystem/organizational-structure-cs"}
              ]}]
            }
            """;

    private final OrganizationRemoteMapper mapper = new OrganizationRemoteMapper();

    private RemoteOrganizationPayload payload() {
        return JsonMapper.builder().build().readValue(DHP_ORGANIZATION, RemoteOrganizationPayload.class);
    }

    @Test
    void toEntityTakesRussianNameFromNameTranslationExtension() {
        Organization entity = mapper.toEntity(payload(), "dhp");

        assertThat(entity.getNameUz()).isEqualTo("Yagona Integrator-uzinfocom Mchj");
        assertThat(entity.getNameRu()).isEqualTo("ООО \"Единый интегратор-uzinfocom\"");
        assertThat(entity.getNameUzCyril()).isNull();
        assertThat(entity.getNameKaa()).isNull();
    }

    @Test
    void toEntityStoresTinAndEveryIdentifier() {
        Organization entity = mapper.toEntity(payload(), "dhp");

        assertThat(entity.getTin()).isEqualTo("204118319");
        assertThat(entity.getIdentifiers())
                .extracting(OrganizationIdentifier::getSystem, OrganizationIdentifier::getValue,
                        OrganizationIdentifier::getTypeCode)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(
                                "https://dhp.uz/fhir/core/sid/org/uz/soliq", "204118319", "TAX"),
                        org.assertj.core.groups.Tuple.tuple(
                                "https://dhp.uz/fhir/core/sid/org/uz/argos", "859", "XX"),
                        org.assertj.core.groups.Tuple.tuple(
                                "https://dhp.uz/fhir/core/sid/org/uz/dhp", "HOR-G5H3KY4-S", "XX"));
    }

    @Test
    void toEntityDerivesLevelAndMedicalTypeFromDhpClassifiersAndKeepsRawCodes() {
        Organization entity = mapper.toEntity(payload(), "dhp");

        assertThat(entity.getLevelType()).isEqualTo(OrganizationLevel.REPUBLICAN);
        assertThat(entity.getMedicalType()).isEqualTo(MedicalType.OTHER);
        assertThat(entity.getTypeCodings())
                .extracting(OrganizationTypeCoding::getCode)
                .containsExactlyInAnyOrder("I_1", "999");
    }

    @Test
    void toEntityMapsDistrictFamilyPolyclinicAndReadsCoverageAreaAsRegion() {
        RemoteOrganizationPayload polyclinic = JsonMapper.builder().build()
                .readValue(DHP_DISTRICT_POLYCLINIC, RemoteOrganizationPayload.class);

        Organization entity = mapper.toEntity(polyclinic, "dhp");

        assertThat(entity.getLevelType()).isEqualTo(OrganizationLevel.DISTRICT);
        assertThat(entity.getMedicalType()).isEqualTo(MedicalType.FAMILY_POLYCLINIC);
        assertThat(entity.getRegionCode()).isEqualTo("1703");
    }

    @Test
    void ownershipSubordinationGroupsAndUnknownStructureCodesFallBackSafely() {
        assertThat(DhpOrganizationTypeMapping.level("I_5")).contains(OrganizationLevel.NOT_DEFINED);
        assertThat(DhpOrganizationTypeMapping.level("I_9")).isEmpty();
        assertThat(DhpOrganizationTypeMapping.medicalType("128")).isEmpty();
        assertThat(DhpOrganizationTypeMapping.medicalType("415")).contains(MedicalType.REHABILITATION_CENTER);
        assertThat(DhpOrganizationTypeMapping.medicalType("411")).contains(MedicalType.SANATORIUM);
    }

    @Test
    void updateEntityReplacesIdentifiersAndTin() {
        Organization entity = mapper.toEntity(payload(), "dhp");
        entity.setTin("stale");
        entity.getIdentifiers().clear();
        entity.getIdentifiers().add(new OrganizationIdentifier("old-system", "old", null));

        mapper.updateEntity(entity, payload(), "dhp");

        assertThat(entity.getTin()).isEqualTo("204118319");
        assertThat(entity.getIdentifiers()).hasSize(3)
                .noneMatch(identifier -> "old-system".equals(identifier.getSystem()));
        assertThat(entity.getNameRu()).isEqualTo("ООО \"Единый интегратор-uzinfocom\"");
    }
}
