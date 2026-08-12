package uz.uzinfocom.isemid.features.act.services.impls;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.mappers.ActToLisDtoConverter;
import uz.uzinfocom.isemid.features.act.mappers.EMisToLisRequestMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.common.DocumentType;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.act.specifications.ActSpecification;
import uz.uzinfocom.isemid.features.act.strategy.impls.UpdateActService;
import uz.uzinfocom.isemid.features.user.models.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {

    private static final String ORGANIZATION_ID_HEADER = "Organization-Id";

    private final ActRepository actRepository;
    private final UpdateActService updateActService;
    private final ActToLisDtoConverter actToLisDtoConverter;
    private final EMisToLisRequestMapper emisToLisRequestMapper;
    private final MessageUtil messageUtil;
    private final IntegrationUrlFactory integrationUrlFactory;

    @Qualifier("integrationRestClient")
    private final RestClient integrationRestClient;

    @Override
    public Page<Act> filter(int page, int size, Long cardId) {
        Specification<Act> spec = ActSpecification.build(cardId);
        return actRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Act getById(long id) {
        return actRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)
                ));
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
            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.internal_server"),
                    e
            );
        }
    }

    @Override
    public void sendToLis(LisRequest lisRequest, UUID organizationUuid, String token) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Act act = actRepository.findWithDetailsForLisById(lisRequest.actId())
                .orElseThrow(() -> new ApiException(
                        NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", lisRequest.actId())
                ));

        markAsPending(act);

        try {
            String redirectUrl = integrationUrlFactory
                    .lisCallbackUrl(act.getId())
                    .toString();

            ActLisDto actLisDto = actToLisDtoConverter.convert(act);

            LisActPushRequest body = emisToLisRequestMapper
                    .map(lisRequest, actLisDto)
                    .withFullNameOfDoctor(user.getFullName())
                    .withRedirectUrl(redirectUrl);

            URI lisCreateActUrl = integrationUrlFactory.lisCreateActUrl(
                    String.valueOf(lisRequest.lisLabId()),
                    String.valueOf(act.getId()),
                    lisRequest.isAllowedDuplicate()
            );

            HttpHeaders headers = buildLisHeaders(organizationUuid, token);

            log.info(
                    "Sending act to LIS. actId={}, lisLabId={}, redirectUrl={}, uri={}",
                    act.getId(),
                    lisRequest.lisLabId(),
                    redirectUrl,
                    lisCreateActUrl
            );

            Long lisActId = integrationRestClient
                    .post()
                    .uri(lisCreateActUrl)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(body)
                    .retrieve()
                    .body(Long.class);

            if (lisActId == null) {
                throw new IllegalStateException("LIS response does not contain act ID");
            }

            markAsSent(act, lisActId);

            log.info("Act successfully sent to LIS. actId={}, lisActId={}", act.getId(), lisActId);

        } catch (Exception e) {
            markAsFailed(act);

            log.error("sendToLis failed. actId={}", act.getId(), e);

            String message = e.getMessage() != null
                    ? e.getMessage()
                    : e.getClass().getSimpleName();

            throw new ApiException(INTERNAL_SERVER_ERROR, message);
        }
    }

    private HttpHeaders buildLisHeaders(UUID organizationUuid, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ORGANIZATION_ID_HEADER, organizationUuid.toString());

        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(normalizeBearerToken(token));
        }

        return headers;
    }

    private String normalizeBearerToken(String token) {
        String value = token.trim();

        if (value.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            return value.substring("Bearer ".length()).trim();
        }

        return value;
    }

    private void markAsPending(Act act) {
        act.setLisAttempt(act.getLisAttempt() + 1);
        act.setStatus(ActStatus.PENDING);
        actRepository.save(act);
    }

    private void markAsSent(Act act, Long lisActId) {
        act.setLisActId(lisActId);
        act.setStatus(ActStatus.SENT);
        act.setLisSentDate(LocalDateTime.now());
        actRepository.save(act);
    }

    private void markAsFailed(Act act) {
        act.setStatus(ActStatus.FAILED);
        actRepository.save(act);
    }

    @Override
    public void handleLisResponse(Long id, DocumentType documentType, Map<String, Object> document) {
        Act act = getById(id);
        updateActService.attachLisDocument(act, documentType, document);
        actRepository.save(act);
    }
}