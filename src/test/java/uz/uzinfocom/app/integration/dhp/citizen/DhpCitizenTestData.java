package uz.uzinfocom.app.integration.dhp.citizen;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** A real DHP {@code Patient/$populate} response (as returned by the playground for NI lookups). */
public final class DhpCitizenTestData {

    public static final JsonMapper JSON = JsonMapper.builder().build();

    public static final String NI = "32501852930042";

    public static final String PATIENT_JSON = """
            {
              "active": true,
              "address": [
                {
                  "city": "17010007",
                  "country": "UZ",
                  "district": "1703203",
                  "line": ["Бешпахлавон МФЙ, Шодлик кучаси, 8-уй"],
                  "state": "1703",
                  "text": "Бешпахлавон МФЙ, Шодлик кучаси, 8-уй",
                  "type": "physical",
                  "use": "home"
                }
              ],
              "birthDate": "1985-01-25",
              "gender": "male",
              "id": "e51a1e86-ac1f-4878-a3c0-6b246dbd0d1e",
              "identifier": [
                {
                  "system": "https://dhp.uz/fhir/core/sid/pid/uz/ni",
                  "type": {"coding": [{"code": "NI", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                  "use": "official",
                  "value": "32501852930042"
                },
                {
                  "period": {"end": "2033-04-25T00:00:00+00:00", "start": "2023-04-26T00:00:00+00:00"},
                  "system": "https://dhp.uz/fhir/core/sid/pid/uz/ppn/local",
                  "type": {"coding": [{"code": "PPN", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                  "use": "official",
                  "value": "AD3131830"
                },
                {
                  "period": {"end": "2023-04-21T00:00:00+00:00", "start": "2013-04-22T00:00:00+00:00"},
                  "system": "https://dhp.uz/fhir/core/sid/pid/uz/ppn/local",
                  "type": {"coding": [{"code": "PPN", "system": "http://terminology.hl7.org/CodeSystem/v2-0203"}]},
                  "use": "old",
                  "value": "AA1301269"
                }
              ],
              "name": [
                {"family": "Karimov", "given": ["Kamoliddin", "Xaydarkulovich"], "use": "official"},
                {"family": "Каримов", "given": ["Камолиддин", "Хайдаркулович"], "use": "usual"}
              ],
              "resourceType": "Patient"
            }
            """;

    private DhpCitizenTestData() {
    }

    public static JsonNode patient() {
        return JSON.readTree(PATIENT_JSON);
    }
}
