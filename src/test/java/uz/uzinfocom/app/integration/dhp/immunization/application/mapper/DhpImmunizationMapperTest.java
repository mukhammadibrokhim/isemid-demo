package uz.uzinfocom.app.integration.dhp.immunization.application.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.dhp.immunization.web.dto.ImmunizationItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DhpImmunizationMapperTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private static final String BUNDLE = """
            {
              "resourceType": "Bundle",
              "type": "searchset",
              "entry": [
                {"resource": {
                  "resourceType": "Immunization",
                  "id": "imm-1",
                  "status": "completed",
                  "vaccineCode": {"coding": [{"system": "urn:cvx", "code": "19", "display": "BCG"}], "text": "BCG vaccine"},
                  "patient": {"reference": "Patient/p1"},
                  "occurrenceDateTime": "2024-03-01T09:30:00+05:00",
                  "lotNumber": "LOT-77",
                  "expirationDate": "2026-01-31",
                  "doseQuantity": {"value": 0.5, "unit": "milliliter", "system": "http://unitsofmeasure.org", "code": "mL"},
                  "performer": [{"actor": {"display": "Dr. Karimov"}}],
                  "protocolApplied": [{"doseNumber": "2", "targetDisease": [{"coding": [{"code": "56717001", "display": "Tuberculosis"}]}]}]
                }},
                {"resource": {
                  "resourceType": "Immunization",
                  "id": "imm-2",
                  "status": "completed",
                  "vaccineCode": {"coding": [{"code": "08", "display": "Hep B"}]},
                  "occurrenceDateTime": "2025-06-10"
                }},
                {"resource": {"resourceType": "Immunization", "id": "imm-3", "status": "entered-in-error",
                  "vaccineCode": {"text": "Retracted"}, "occurrenceDateTime": "2025-07-01"}},
                {"resource": {"resourceType": "OperationOutcome", "id": "not-an-immunization"}},
                {"resource": {"resourceType": "Immunization", "id": "imm-4", "status": "completed",
                  "vaccineCode": {"text": "No date"}, "occurrenceString": "as a baby"}}
              ]
            }""";

    @Test
    void mapsAFhirR5ImmunizationIntoOurShape() {
        ImmunizationItem bcg = byId(map(BUNDLE), "imm-1");

        assertThat(bcg.status()).isEqualTo("completed");
        assertThat(bcg.vaccineCode()).isEqualTo("19");
        assertThat(bcg.vaccinationName()).isEqualTo("BCG vaccine");
        assertThat(bcg.serialNumber()).isEqualTo("LOT-77");
        assertThat(bcg.vaccinationDate()).isEqualTo(LocalDateTime.of(2024, 3, 1, 9, 30));
        assertThat(bcg.expirationDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(bcg.doseVolume()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(bcg.doseUnit()).isEqualTo("mL");
        assertThat(bcg.doseNumber()).isEqualTo(2);
        assertThat(bcg.targetDiseases()).containsExactly("Tuberculosis");
        assertThat(bcg.performerName()).isEqualTo("Dr. Karimov");
    }

    @Test
    void fallsBackToCodingDisplayAndDateOnlyValues() {
        ImmunizationItem hepB = byId(map(BUNDLE), "imm-2");

        assertThat(hepB.vaccinationName()).isEqualTo("Hep B");
        assertThat(hepB.vaccinationDate()).isEqualTo(LocalDate.of(2025, 6, 10).atStartOfDay());
        assertThat(hepB.serialNumber()).isNull();
        assertThat(hepB.doseVolume()).isNull();
        assertThat(hepB.targetDiseases()).isEmpty();
    }

    @Test
    void dropsEnteredInErrorAndNonImmunizationEntries() {
        assertThat(map(BUNDLE)).extracting(ImmunizationItem::fhirId)
                .containsExactlyInAnyOrder("imm-1", "imm-2", "imm-4");
    }

    @Test
    void ordersNewestFirstWithUndatedLast() {
        assertThat(map(BUNDLE)).extracting(ImmunizationItem::fhirId)
                .containsExactly("imm-2", "imm-1", "imm-4");
    }

    @Test
    void mergesSeveralBundlesAndToleratesEmptyOnes() {
        List<ImmunizationItem> items = DhpImmunizationMapper.map(List.of(
                parse("{\"resourceType\":\"Bundle\",\"entry\":[{\"resource\":{\"resourceType\":\"Immunization\","
                        + "\"id\":\"a\",\"status\":\"completed\",\"occurrenceDateTime\":\"2020-01-01\"}}]}"),
                parse("{\"resourceType\":\"Bundle\",\"total\":0}"),
                parse("{\"resourceType\":\"Bundle\",\"entry\":[{\"resource\":{\"resourceType\":\"Immunization\","
                        + "\"id\":\"b\",\"status\":\"completed\",\"occurrenceDateTime\":\"2021-01-01\"}}]}")));

        assertThat(items).extracting(ImmunizationItem::fhirId).containsExactly("b", "a");
    }

    @Test
    void noBundlesMeansNoItems() {
        assertThat(DhpImmunizationMapper.map(List.of())).isEmpty();
    }

    private List<ImmunizationItem> map(String bundle) {
        return DhpImmunizationMapper.map(List.of(parse(bundle)));
    }

    private JsonNode parse(String json) {
        return jsonMapper.readTree(json);
    }

    private static ImmunizationItem byId(List<ImmunizationItem> items, String id) {
        return items.stream().filter(item -> id.equals(item.fhirId())).findFirst().orElseThrow();
    }
}
