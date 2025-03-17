package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct156Strategy implements UpdateActStrategy {

    private final CatalogService catalogService;
    private final ActRepository actRepository;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }

    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Act156 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }

        return new ApiResponse<>(true, "Success", null);
    }
}
