package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.UUID;

public interface Journal060Service {
    ApiResponsePage getAllJournal060Filter(
            int page,
            int size,
            Gender gender,
            String request,
            String lastName,
            String reporterFullName,
            String nnuzb,
            UUID senderOrgUuid,
            String receiverOrgUuid,
            String fullURL
    );

    ApiResponse<?> getById(long id);
}
