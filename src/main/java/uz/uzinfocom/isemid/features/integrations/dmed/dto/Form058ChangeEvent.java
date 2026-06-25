package uz.uzinfocom.isemid.features.integrations.dmed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;

public record Form058ChangeEvent(@JsonProperty("uuid") UUID formUUID, FormStatus status,
                                 String organization, @JsonProperty("updated_by") String updatedBy) {}