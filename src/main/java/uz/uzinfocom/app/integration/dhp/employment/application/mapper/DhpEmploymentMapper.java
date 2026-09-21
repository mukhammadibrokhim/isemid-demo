package uz.uzinfocom.app.integration.dhp.employment.application.mapper;

import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;
import uz.uzinfocom.app.integration.dhp.employment.web.dto.EmploymentItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps the egov MOL {@code employment/by-ni} payload into {@link EmploymentItem}s.
 *
 * <p>Confirmed live against the playground 2026-09-21: the response is a
 * JSON-RPC envelope, {@code {"result":{"positions":[{"org":...,"org_tin":...,
 * "position":...,"begin_date":...}, ...]}}}. This still reads defensively -
 * the list may sit under a common wrapper key, an item's employer may be a
 * nested object, and every field is looked up under several likely names
 * (matched ignoring case, {@code _} and {@code -}) - since prod may differ
 * from the playground and no {@code end_date} sample has been seen yet
 * (nothing in the captured response had ended). Every item also carries the
 * untouched source record in {@code raw}.
 */
public final class DhpEmploymentMapper {

    private static final List<String> LIST_KEYS = List.of(
            "data", "items", "content", "employments", "employment", "positions", "records", "results", "result");
    private static final List<String> EMPLOYER_OBJECT_KEYS = List.of(
            "organization", "employer", "company", "workplace", "org");

    private static final List<String> NAME_KEYS = List.of(
            "organizationname", "orgname", "companyname", "employername", "employer", "company",
            "workplace", "workplacename", "organization", "org");
    /** Inside a nested employer object a bare "name" is the employer's; on the record itself it could be the person's. */
    private static final List<String> EMPLOYER_NAME_KEYS = List.of(
            "organizationname", "orgname", "companyname", "employername", "name");
    private static final List<String> TIN_KEYS = List.of(
            "organizationtin", "orgtin", "employertin", "companytin", "tin", "stir", "inn");
    private static final List<String> POSITION_KEYS = List.of(
            "position", "positionname", "jobtitle", "job", "profession", "occupation", "post");
    private static final List<String> START_KEYS = List.of(
            "startdate", "hiredate", "employmentdate", "datefrom", "begindate", "datestart", "from");
    private static final List<String> END_KEYS = List.of(
            "enddate", "dismissaldate", "firedate", "terminationdate", "dateto", "dateend", "to");
    private static final List<String> ACTIVE_KEYS = List.of(
            "isactive", "active", "current", "iscurrent", "working");

    private DhpEmploymentMapper() {
    }

    public static List<EmploymentItem> map(JsonNode payload, LocalDate today) {
        List<EmploymentItem> items = new ArrayList<>();

        for (JsonNode record : records(payload)) {
            EmploymentItem item = toItem(record, today);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    private static List<JsonNode> records(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return List.of();
        }
        if (payload.isArray()) {
            return elements(payload);
        }
        if (!payload.isObject()) {
            return List.of();
        }

        for (String key : LIST_KEYS) {
            JsonNode candidate = field(payload, List.of(key));
            if (candidate == null) {
                continue;
            }
            if (candidate.isArray()) {
                return elements(candidate);
            }
            if (candidate.isObject()) {
                return records(candidate);
            }
        }

        return List.of(payload);
    }

    private static List<JsonNode> elements(JsonNode array) {
        List<JsonNode> elements = new ArrayList<>();
        for (JsonNode element : array) {
            if (element != null && element.isObject()) {
                elements.add(element);
            }
        }
        return elements;
    }

    private static EmploymentItem toItem(JsonNode record, LocalDate today) {
        JsonNode employer = employerObject(record);

        String organizationName = firstText(employer, EMPLOYER_NAME_KEYS);
        if (organizationName == null) {
            organizationName = firstText(record, NAME_KEYS);
        }
        String organizationTin = firstText(employer, TIN_KEYS);
        if (organizationTin == null) {
            organizationTin = firstText(record, TIN_KEYS);
        }
        String position = firstText(record, POSITION_KEYS);
        LocalDate startDate = DhpJson.date(firstText(record, START_KEYS));
        LocalDate endDate = DhpJson.date(firstText(record, END_KEYS));

        if (organizationName == null && organizationTin == null && position == null
                && startDate == null && endDate == null) {
            return null;
        }

        return new EmploymentItem(
                organizationName,
                organizationTin,
                position,
                startDate,
                endDate,
                isCurrent(record, endDate, today),
                record
        );
    }

    private static boolean isCurrent(JsonNode record, LocalDate endDate, LocalDate today) {
        JsonNode flag = field(record, ACTIVE_KEYS);
        if (flag != null && flag.isBoolean()) {
            return flag.asBoolean();
        }

        return endDate == null || endDate.isAfter(today);
    }

    /** The employer when it is a nested object under a well-known key, otherwise {@code null}. */
    private static JsonNode employerObject(JsonNode record) {
        for (String key : EMPLOYER_OBJECT_KEYS) {
            JsonNode candidate = field(record, List.of(key));
            if (candidate != null && candidate.isObject()) {
                return candidate;
            }
        }
        return null;
    }

    private static String firstText(JsonNode node, List<String> candidates) {
        for (String candidate : candidates) {
            String value = DhpJson.text(field(node, List.of(candidate)));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** First present, non-null property whose normalized name is in {@code candidates} (checked in order). */
    private static JsonNode field(JsonNode node, List<String> candidates) {
        if (node == null || !node.isObject()) {
            return null;
        }

        for (String candidate : candidates) {
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                if (!property.getValue().isNull() && DhpJson.normalizeKey(property.getKey()).equals(candidate)) {
                    return property.getValue();
                }
            }
        }

        return null;
    }
}
