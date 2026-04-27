package uz.uzinfocom.isemid.features.act.services.impls;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.ISEMID_CALLBACK;
import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.LIS_ACT_API;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.RestClientUtil;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.mappers.ActToLisDtoConverter;
import uz.uzinfocom.isemid.features.act.mappers.EMisToLisRequestMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.act.specifications.ActSpecification;
import uz.uzinfocom.isemid.features.act.strategy.impls.UpdateActService;
import uz.uzinfocom.isemid.features.user.models.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {
    private final ActRepository actRepository;
    private final UpdateActService updateActService;
    private final ActToLisDtoConverter actToLisDtoConverter;
    private final EMisToLisRequestMapper emisToLisRequestMapper;
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

        User user = (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();

        Act act = actRepository.findWithDetailsForLisById(lisRequest.actId())
            .orElseThrow(() -> new ApiException(NOT_FOUND,
                messageUtil.get("error.not_found_by_id", lisRequest.actId())));

        act.setLisAttempt(act.getLisAttempt() + 1);
        act.setStatus(ActStatus.PENDING);
        actRepository.save(act);

        try {
            String redirectUrl = ISEMID_CALLBACK + act.getId();

            ActLisDto actLisDto = actToLisDtoConverter.convert(act);

            LisActPushRequest body = emisToLisRequestMapper
                .map(lisRequest, actLisDto)
                .withFullNameOfDoctor(user.getFullName())
                .withRedirectUrl(redirectUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Organization-Id", organizationUuid.toString());

            String url = LIS_ACT_API.formatted(lisRequest.lisLabId(), act.getId());

            Void lisResponse = RestClientUtil.makeRequest(url, HttpMethod.POST, "", body, headers, Void.class, messageUtil);


            log.info("LIS response: {}", lisResponse);

            act.setStatus(ActStatus.SENT);
            act.setLisSentDate(LocalDateTime.now());
            actRepository.save(act);

        } catch (Exception e) {
            act.setStatus(ActStatus.FAILED);
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

