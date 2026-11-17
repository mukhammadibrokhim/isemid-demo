package uz.uzinfocom.app.integration.dhp.citizen.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressType;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.integration.dhp.common.support.DhpJson;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the FHIR {@code Patient.address[]} onto the same {@link CitizenAddressResponse}
 * the API2 {@code v3/citizenAddress} path produces. DHP carries SOATO codes directly:
 * {@code state} = region, {@code district} = district, {@code city} = neighborhood
 * (mahalla); each is resolved through {@link ReferenceLookupService} the way
 * {@code CitizenAddressMapper} resolves API2's {@code soato_id}. The address line is
 * the whole {@code text} (it already contains the neighborhood name and street).
 */
@Component
@RequiredArgsConstructor
public class DhpPatientAddressMapper {

    private final ReferenceLookupService referenceLookupService;

    public List<CitizenAddressResponse> map(JsonNode patient) {
        JsonNode addresses = DhpJson.child(patient, "address");
        if (addresses == null || !addresses.isArray()) {
            return List.of();
        }

        List<CitizenAddressResponse> result = new ArrayList<>();
        for (JsonNode address : addresses) {
            CitizenAddressType type = type(DhpJson.text(address, "use"));
            if (type != null) {
                result.add(toResponse(type, address));
            }
        }
        return List.copyOf(result);
    }

    /** {@code home}/unspecified = registered (permanent) address, {@code temp} = temporary; other uses ({@code old}, {@code work}...) are not addresses of residence. */
    private static CitizenAddressType type(String use) {
        if (use == null || "home".equals(use)) {
            return CitizenAddressType.PERMANENT;
        }
        return "temp".equals(use) ? CitizenAddressType.TEMPORARY : null;
    }

    private CitizenAddressResponse toResponse(CitizenAddressType type, JsonNode address) {
        String state = DhpJson.text(address, "state");
        String districtCode = DhpJson.text(address, "district");
        String city = DhpJson.text(address, "city");

        ReferenceItem region = state == null ? null : referenceLookupService.findRegion(state);
        ReferenceItem district = districtCode == null ? null : referenceLookupService.findDistrict(districtCode);
        ReferenceItem neighborhood = city == null ? null : referenceLookupService.findNeighborhood(city);

        LocalDate registered = DhpJson.date(DhpJson.text(DhpJson.child(address, "period"), "start"));

        return new CitizenAddressResponse(
                type,
                region == null ? null : region.code(),
                region == null ? null : referenceLookupService.getRegionName(region.code()),
                district == null ? null : district.code(),
                district == null ? null : referenceLookupService.getDistrictName(district.code()),
                neighborhood == null ? null : neighborhood.tin(),
                neighborhood == null ? null : referenceLookupService.getNeighborhoodName(neighborhood.code()),
                streetAddress(address),
                null,
                registered == null ? null : registered.toString()
        );
    }

    private static String streetAddress(JsonNode address) {
        String text = DhpJson.text(address, "text");
        if (text != null) {
            return text;
        }

        JsonNode lines = DhpJson.child(address, "line");
        if (lines == null || !lines.isArray()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (JsonNode line : lines) {
            String value = DhpJson.text(line);
            if (value != null) {
                parts.add(value);
            }
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }
}
