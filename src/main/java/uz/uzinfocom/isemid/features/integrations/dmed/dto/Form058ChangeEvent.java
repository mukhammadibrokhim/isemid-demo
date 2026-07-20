package uz.uzinfocom.isemid.features.integrations.dmed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;

import java.util.UUID;

public record Form058ChangeEvent(@JsonProperty("uuid") UUID formUUID, FormStatus status, String reason,
                                 String organization, @JsonProperty("updated_by") String updatedBy) {}