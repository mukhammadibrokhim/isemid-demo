package uz.uzinfocom.app.integration.dhp.citizen.application.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.application.mapper.CitizenResponseMapper;
import uz.uzinfocom.app.integration.dhp.citizen.DhpCitizenTestData;

import static org.assertj.core.api.Assertions.assertThat;

class DhpPatientCitizenMapperTest {

    private final DhpPatientCitizenMapper mapper = new DhpPatientCitizenMapper(DhpCitizenTestData.JSON);

    @Test
    void wrapsTheCitizenInTheApi2PayloadEnvelope() {
        JsonNode payload = mapper.toApi2Payload(DhpCitizenTestData.patient());

        assertThat(CitizenResponseMapper.result(payload)).isEqualTo("1");
        assertThat(CitizenResponseMapper.comments(payload)).isEmpty();
        assertThat(payload.get("data").isArray()).isTrue();
        assertThat(payload.get("data")).hasSize(1);
    }

    @Test
    void producesTheSameCitizenFieldsAsApi2AfterTheExistingResponseMapper() {
        JsonNode citizen = CitizenResponseMapper
                .data(mapper.toApi2Payload(DhpCitizenTestData.patient()))
                .get(0);

        assertThat(citizen.get("current_pinpp").asString()).isEqualTo("32501852930042");
        assertThat(citizen.get("pinpps")).hasSize(1);
        assertThat(citizen.get("pinpps").get(0).asString()).isEqualTo("32501852930042");
        assertThat(citizen.get("current_document").asString()).isEqualTo("AD3131830");

        assertThat(citizen.get("surnamelat").asString()).isEqualTo("Karimov");
        assertThat(citizen.get("namelat").asString()).isEqualTo("Kamoliddin");
        assertThat(citizen.get("patronymlat").asString()).isEqualTo("Xaydarkulovich");
        assertThat(citizen.get("surnamecyr").asString()).isEqualTo("Каримов");
        assertThat(citizen.get("namecyr").asString()).isEqualTo("Камолиддин");
        assertThat(citizen.get("patronymcyr").asString()).isEqualTo("Хайдаркулович");

        assertThat(citizen.get("birth_date").asString()).isEqualTo("1985-01-25");
        assertThat(citizen.get("gender").asString()).isEqualTo("male");
        assertThat(citizen.has("sex")).isFalse();
    }

    @Test
    void mapsIdentifiersToDocumentsWithValidityPeriodAndStatus() {
        JsonNode documents = CitizenResponseMapper
                .data(mapper.toApi2Payload(DhpCitizenTestData.patient()))
                .get(0)
                .get("documents");

        assertThat(documents).hasSize(2);

        JsonNode current = documents.get(0);
        assertThat(current.get("document").asString()).isEqualTo("AD3131830");
        assertThat(current.get("type").asString()).isEqualTo("PPN");
        assertThat(current.get("datebegin").asString()).isEqualTo("2023-04-26");
        assertThat(current.get("dateend").asString()).isEqualTo("2033-04-25");
        assertThat(current.get("status").asInt()).isEqualTo(2);

        JsonNode old = documents.get(1);
        assertThat(old.get("document").asString()).isEqualTo("AA1301269");
        assertThat(old.get("datebegin").asString()).isEqualTo("2013-04-22");
        assertThat(old.get("dateend").asString()).isEqualTo("2023-04-21");
        assertThat(old.get("status").asInt()).isEqualTo(4);
    }

    @Test
    void keepsFieldsDhpDoesNotProvideAsNullSoTheShapeIsUnchanged() {
        JsonNode citizen = CitizenResponseMapper
                .data(mapper.toApi2Payload(DhpCitizenTestData.patient()))
                .get(0);

        for (String field : new String[]{
                "transaction_id", "birthplace", "birthcountry", "birthcountryid", "livestatus",
                "nationality", "nationalityid", "citizenship", "citizenshipid"}) {
            assertThat(citizen.has(field)).as(field).isTrue();
            assertThat(citizen.get(field).isNull()).as(field).isTrue();
        }
    }

    @Test
    void femaleAndUnknownGender() {
        JsonNode female = DhpCitizenTestData.JSON.readTree("{\"resourceType\":\"Patient\",\"gender\":\"female\"}");
        JsonNode unknown = DhpCitizenTestData.JSON.readTree("{\"resourceType\":\"Patient\"}");

        assertThat(CitizenResponseMapper.data(mapper.toApi2Payload(female)).get(0).get("gender").asString())
                .isEqualTo("female");
        assertThat(CitizenResponseMapper.data(mapper.toApi2Payload(unknown)).get(0).get("gender").isNull())
                .isTrue();
    }
}
