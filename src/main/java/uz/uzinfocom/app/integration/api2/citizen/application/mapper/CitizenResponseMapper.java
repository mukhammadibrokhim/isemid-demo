package uz.uzinfocom.app.integration.api2.citizen.application.mapper;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

public final class CitizenResponseMapper {

    private static final Map<String, String> DOCUMENT_TYPE_MAPPING =
            Map.ofEntries(
                    Map.entry("IDMS_RECV_CITIZ_DOCUMENTS", "PPN"),
                    Map.entry("IDMS_RECV_IP_DOCUMENTS", "TPPN"),
                    Map.entry("IDMS_RECV_LBG_DOCUMENTS", "FID"),
                    Map.entry("IDMS_RECV_MVD_IDCARD_CITIZEN", "CZ"),
                    Map.entry("IDMS_RECV_MVD_IDCARD_FOREIGN", "CZF"),
                    Map.entry("IDMS_RECV_MVD_IDCARD_LBG", "CZF"),
                    Map.entry("IDMS_RECV_MVD_IDCARD_NEWBORN", "CZ"),
                    Map.entry("IDMS_RECV_MJ_BIRTH_CERTS", "BCT")
            );

    private CitizenResponseMapper() {
    }

    public static String result(JsonNode payload) {
        if (payload == null) {
            return null;
        }

        return textOrNull(payload.get("result"));
    }

    public static String comments(JsonNode payload) {
        if (payload == null) {
            return null;
        }

        return textOrNull(payload.get("comments"));
    }

    public static JsonNode data(JsonNode payload) {
        if (payload == null) {
            return null;
        }

        JsonNode data = payload.get("data");
        if (data == null || data.isNull()) {
            return null;
        }

        JsonNode mappedData = data.deepCopy();
        mapCitizens(mappedData);

        return mappedData;
    }

    private static void mapCitizens(JsonNode citizensNode) {
        if (citizensNode == null || !citizensNode.isArray()) {
            return;
        }

        for (JsonNode citizenNode : citizensNode) {
            if (citizenNode instanceof ObjectNode citizenObject) {
                mapDocuments(citizenObject.get("documents"));
                mapGender(citizenObject);
            }
        }
    }

    private static void mapDocuments(JsonNode documentsNode) {
        if (documentsNode == null || !documentsNode.isArray()) {
            return;
        }

        for (JsonNode documentNode : documentsNode) {
            if (documentNode instanceof ObjectNode documentObject && documentObject.has("type")) {
                String mappedType = mapDocumentType(textOrNull(documentObject.get("type")));

                if (mappedType == null) {
                    documentObject.putNull("type");
                } else {
                    documentObject.put("type", mappedType);
                }
            }
        }
    }

    private static void mapGender(ObjectNode citizenObject) {
        JsonNode sexNode = citizenObject.get("sex");
        citizenObject.remove("sex");

        String gender = mapGender(integerOrNull(sexNode));

        if (gender == null) {
            citizenObject.putNull("gender");
        } else {
            citizenObject.put("gender", gender);
        }
    }

    private static String mapDocumentType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return null;
        }

        return DOCUMENT_TYPE_MAPPING.getOrDefault(
                sourceType,
                sourceType
        );
    }

    private static String mapGender(Integer sex) {
        if (sex == null) {
            return null;
        }

        return sex == 1
                ? "male"
                : "female";
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private static Integer integerOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isNumber()) {
            return node.asInt();
        }

        return null;
    }
}
