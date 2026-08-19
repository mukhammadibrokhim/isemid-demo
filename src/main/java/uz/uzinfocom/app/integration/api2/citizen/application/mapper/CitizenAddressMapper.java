package uz.uzinfocom.app.integration.api2.citizen.application.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressType;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the raw v3/citizenAddress payload (PascalCase, {@code Data.PermanentRegistration} /
 * {@code Data.TemproaryRegistrations}) onto {@link CitizenAddressResponse}. Region/District are
 * resolved via the numeric {@code soato_id} API2 embeds on each node; Maxalla (neighborhood) is
 * resolved via its {@code Guid}, matched against {@code ref_neighborhood.uzcad_registry_code}
 * (see that column's migration for where this value comes from and why it's a softer,
 * not-guaranteed-unique key unlike soato_id/tin). Any of the three can come back unresolved
 * (code/name null, or Maxalla's raw name passed through as-is) if the source data has no match -
 * that's expected, not an error, since this whole lookup is a best-effort enrichment.
 */
@Component
@RequiredArgsConstructor
public class CitizenAddressMapper {

    private final ReferenceLookupService referenceLookupService;

    public List<CitizenAddressResponse> map(JsonNode payload) {
        if (payload == null) {
            return List.of();
        }

        JsonNode data = payload.get("Data");
        if (data == null || data.isNull()) {
            return List.of();
        }

        List<CitizenAddressResponse> result = new ArrayList<>();
        addIfPresent(result, CitizenAddressType.PERMANENT, data.get("PermanentRegistration"));

        JsonNode temporary = data.get("TemproaryRegistrations");
        if (temporary != null && temporary.isArray()) {
            for (JsonNode node : temporary) {
                addIfPresent(result, CitizenAddressType.TEMPORARY, node);
            }
        } else {
            addIfPresent(result, CitizenAddressType.TEMPORARY, temporary);
        }

        return List.copyOf(result);
    }

    private void addIfPresent(List<CitizenAddressResponse> result, CitizenAddressType type, JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) {
            return;
        }

        result.add(toResponse(type, node));
    }

    private CitizenAddressResponse toResponse(CitizenAddressType type, JsonNode node) {
        ReferenceItem region = resolveRegion(node.get("Region"));
        ReferenceItem district = resolveDistrict(node.get("District"));
        ReferenceItem neighborhood = resolveNeighborhood(node.get("Maxalla"));

        String rawNeighborhoodName = textOrNull(node.path("Maxalla").get("Value"));

        return new CitizenAddressResponse(
                type,
                region == null ? null : region.code(),
                region == null ? null : referenceLookupService.getRegionName(region.code()),
                district == null ? null : district.code(),
                district == null ? null : referenceLookupService.getDistrictName(district.code()),
                neighborhood == null ? null : neighborhood.tin(),
                neighborhood == null ? rawNeighborhoodName : referenceLookupService.getNeighborhoodName(neighborhood.code()),
                textOrNull(node.get("Address")),
                textOrNull(node.get("Cadastre")),
                textOrNull(node.get("RegistrationDate"))
        );
    }

    private ReferenceItem resolveRegion(JsonNode regionNode) {
        String soatoId = soatoIdOrNull(regionNode);
        return soatoId == null ? null : referenceLookupService.findRegion(soatoId);
    }

    private ReferenceItem resolveDistrict(JsonNode districtNode) {
        String soatoId = soatoIdOrNull(districtNode);
        return soatoId == null ? null : referenceLookupService.findDistrict(soatoId);
    }

    private ReferenceItem resolveNeighborhood(JsonNode maxallaNode) {
        if (maxallaNode == null || maxallaNode.isNull()) {
            return null;
        }

        String guid = textOrNull(maxallaNode.get("Guid"));
        return guid == null ? null : referenceLookupService.findNeighborhood(guid);
    }

    private String soatoIdOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        JsonNode soatoId = node.get("soato_id");
        if (soatoId == null || soatoId.isNull()) {
            return null;
        }

        return soatoId.isNumber() ? String.valueOf(soatoId.asLong()) : textOrNull(soatoId);
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        return node.asText();
    }
}
