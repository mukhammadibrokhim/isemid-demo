package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.enums.CardStatus;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

import java.util.UUID;

public interface CardService {
    ApiResponse<?> getAllCardsByFilter(
            int page,
            int size,
            String formTypeCode,
            UUID fromOrgId,
            UUID toOrgId,
            CardStatus cardStatus,
            String fullURL
    );

}
