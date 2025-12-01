package uz.uzinfocom.isemid.features.act.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.RestClientUtil;
import uz.uzinfocom.isemid.features.act.dtos.LisPushRequest;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.act.specifications.ActSpecification;
import uz.uzinfocom.isemid.features.act.strategy.impls.UpdateActService;

import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.ISEMID_CALLBACK;
import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.LIS_URL;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {
    private final ActRepository actRepository;
    private final UpdateActService updateActService;
    private final ActMapperFactory actMapperFactory;
    private final MessageUtil messageUtil;

    @Override
    public Page<Act> filter(int page, int size, Long cardId) {
        Specification<Act> spec = ActSpecification.build(cardId);
        return actRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Act getById(long id) {
        return actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Transactional
    @Override
    public Act update(long id, ActRequest reqAct) {
        return updateActService.updateAct(id, reqAct);
    }

    @Override
    public void deleteById(Long id) {
        try {
            actRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }
    }

    @Override
    public void sendToLis(LisRequest lisRequest, UUID organizationUuid, String token) {
        Act act = getById(lisRequest.getActId());

        act.setLisAttempt(act.getLisAttempt() + 1);
        act.setStatus(ActStatus.PENDING);
        actRepository.save(act);

        try {
            ActResponse response = actMapperFactory.toResponse(act);
            response.setUsers(null);

            ObjectMapper mapper = new ObjectMapper();
            String isemidDataJson = mapper.writeValueAsString(response);

            String redirectUrl = ISEMID_CALLBACK + act.getId();

            LisPushRequest body = new LisPushRequest();
            body.setIsemidData(isemidDataJson);
            body.setRedirectUrl(redirectUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Organization-Id", organizationUuid.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LisPushRequest> entity = new HttpEntity<>(body, headers);

            String url = LIS_URL + lisRequest.getLisLabId();
            String lisResponse = RestClientUtil.makeRequest(url, HttpMethod.PUT, token, entity, String.class, messageUtil);

            log.info("LIS response: {}", lisResponse);

            act.setStatus(ActStatus.SENT);
            act.setLisResponse(lisResponse);
            actRepository.save(act);

        } catch (Exception e) {

            act.setStatus(ActStatus.FAILED);
            act.setLisResponse(e.getMessage());
            actRepository.save(act);

            throw new ApiException(INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }


    @Override
    public void handleLisResponse(Long id, String body) {
        Act act = getById(id);

        act.setLisResponse(body);
        act.setStatus(ActStatus.COMPLETED);

        actRepository.save(act);
    }
}

