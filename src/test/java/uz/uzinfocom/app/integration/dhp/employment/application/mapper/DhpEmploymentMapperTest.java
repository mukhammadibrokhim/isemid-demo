package uz.uzinfocom.app.integration.dhp.employment.application.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.dhp.employment.web.dto.EmploymentItem;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the tolerant matching in {@link DhpEmploymentMapper} against both the
 * real egov MOL JSON-RPC shape (confirmed live against the playground
 * 2026-09-21) and representative synthetic shapes, in case prod ever differs.
 */
class DhpEmploymentMapperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 21);
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void mapsTheRealEgovMolJsonRpcShape() {
        List<EmploymentItem> items = map("""
                {"error":null,"id":300491849,"jsonrpc":"2.0","result":{
                  "id":4624952,"name":"FIRSTNAME","surname":"LASTNAME","pnfl":"12345678901234",
                  "result_code":1,"result_message":"OK",
                  "positions":[
                    {"begin_date":"2021-11-08","dep_id":1,"dep_name":"N/A","doc_begin_num":"5",
                     "org":"EXAMPLE LLC","org_id":"111111111","org_tin":"111111111",
                     "position":"Muhandis","position_id":1,"rate":"1.00"},
                    {"begin_date":"2018-01-15","end_date":"2019-12-31","dep_id":2,
                     "org":"OLD CLINIC","org_id":"222222222","org_tin":"222222222",
                     "position":"Intern","position_id":2,"rate":"0.50"}
                  ]
                }}""");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).organizationName()).isEqualTo("EXAMPLE LLC");
        assertThat(items.get(0).organizationTin()).isEqualTo("111111111");
        assertThat(items.get(0).position()).isEqualTo("Muhandis");
        assertThat(items.get(0).startDate()).isEqualTo(LocalDate.of(2021, 11, 8));
        assertThat(items.get(0).endDate()).isNull();
        assertThat(items.get(0).current()).isTrue();
        assertThat(items.get(1).organizationName()).isEqualTo("OLD CLINIC");
        assertThat(items.get(1).endDate()).isEqualTo(LocalDate.of(2019, 12, 31));
        assertThat(items.get(1).current()).isFalse();
    }

    @Test
    void mapsFlatSnakeCaseRecordsFromARootArray() {
        List<EmploymentItem> items = map("""
                [
                  {"organization_name":"ACME LLC","tin":"123456789","position":"Nurse",
                   "start_date":"2020-03-01","end_date":null},
                  {"organization_name":"Old Clinic","tin":"987654321","position":"Intern",
                   "start_date":"2018-01-15","end_date":"2019-12-31"}
                ]""");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).organizationName()).isEqualTo("ACME LLC");
        assertThat(items.get(0).organizationTin()).isEqualTo("123456789");
        assertThat(items.get(0).position()).isEqualTo("Nurse");
        assertThat(items.get(0).startDate()).isEqualTo(LocalDate.of(2020, 3, 1));
        assertThat(items.get(0).endDate()).isNull();
        assertThat(items.get(0).current()).isTrue();
        assertThat(items.get(1).endDate()).isEqualTo(LocalDate.of(2019, 12, 31));
        assertThat(items.get(1).current()).isFalse();
    }

    @Test
    void mapsCamelCaseRecordsWrappedInData() {
        List<EmploymentItem> items = map("""
                {"data":[{"organizationName":"ACME LLC","jobTitle":"Doctor","hireDate":"01.02.2021"}]}""");

        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.organizationName()).isEqualTo("ACME LLC");
            assertThat(item.position()).isEqualTo("Doctor");
            assertThat(item.startDate()).isEqualTo(LocalDate.of(2021, 2, 1));
        });
    }

    @Test
    void readsANestedEmployerObject() {
        List<EmploymentItem> items = map("""
                [{"employer":{"name":"ACME LLC","stir":"123456789"},"position":"Nurse"}]""");

        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.organizationName()).isEqualTo("ACME LLC");
            assertThat(item.organizationTin()).isEqualTo("123456789");
        });
    }

    @Test
    void anExplicitActiveFlagWinsOverTheEndDate() {
        List<EmploymentItem> items = map("""
                [{"organization_name":"ACME","end_date":"2030-01-01","is_active":false}]""");

        assertThat(items).singleElement().satisfies(item -> assertThat(item.current()).isFalse());
    }

    @Test
    void aFutureEndDateStillCountsAsCurrent() {
        List<EmploymentItem> items = map("""
                [{"organization_name":"ACME","end_date":"2030-01-01"}]""");

        assertThat(items).singleElement().satisfies(item -> assertThat(item.current()).isTrue());
    }

    @Test
    void aBareNameOnTheRecordIsNotTakenAsTheEmployer() {
        List<EmploymentItem> items = map("""
                [{"name":"Some Person","position":"Nurse"}]""");

        assertThat(items).singleElement().satisfies(item -> assertThat(item.organizationName()).isNull());
    }

    @Test
    void keepsTheUntouchedSourceRecord() {
        List<EmploymentItem> items = map("""
                [{"organization_name":"ACME","custom_field":"kept"}]""");

        assertThat(items.get(0).raw().get("custom_field").asString()).isEqualTo("kept");
    }

    @Test
    void emptyOrUnrecognisedPayloadsYieldNoItems() {
        assertThat(map("[]")).isEmpty();
        assertThat(map("{}")).isEmpty();
        assertThat(map("{\"result\":\"OK\"}")).isEmpty();
        assertThat(map("null")).isEmpty();
        assertThat(map("[{\"unrelated\":1}]")).isEmpty();
    }

    private List<EmploymentItem> map(String json) {
        JsonNode payload = jsonMapper.readTree(json);
        return DhpEmploymentMapper.map(payload, TODAY);
    }
}
