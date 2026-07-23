package uz.uzinfocom.app.modules.act.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.lis.client.LisActClient;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest;
import uz.uzinfocom.app.integration.lis.client.dto.LisResearchCode;
import uz.uzinfocom.app.integration.lis.client.mapper.ActLisPayloadMapper;
import uz.uzinfocom.app.integration.lis.common.exception.LisException;
import uz.uzinfocom.app.integration.lis.common.support.LisUrlFactory;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.web.dto.request.SendActToLisRequest;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.UUID;

/**
 * Orchestrates one send-to-LIS attempt across three separate
 * {@code ActCommandService} transactions with the actual HTTP call to LIS
 * happening <em>between</em> them, never inside one:
 *
 * <ol>
 *     <li>{@link ActCommandService#markSendingToLis} — commits the act to
 *     {@code SENT} and returns it, so a concurrent second attempt is
 *     rejected by that same status check rather than racing this one;</li>
 *     <li>the LIS call itself, through {@link LisActClient} — not
 *     transactional, so a slow or hanging upstream never holds a database
 *     connection or row lock;</li>
 *     <li>{@link ActCommandService#recordLisSendSuccess} or
 *     {@link ActCommandService#recordLisSendFailure}, depending on how step
 *     2 went.</li>
 * </ol>
 *
 * <p>This class is intentionally not {@code @Transactional} itself — Spring
 * would otherwise wrap the whole method, including the HTTP call, in one
 * connection-holding transaction, exactly what splitting into three
 * {@code ActCommandService} calls is meant to avoid. No transactional method
 * here calls another transactional method on {@code this} — self-invocation
 * bypasses the proxy — so every step here is a call onto a different bean.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActLisSendService {

    private final ActCommandService actCommandService;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final LisActClient lisActClient;
    private final ActLisPayloadMapper payloadMapper;
    private final LisUrlFactory lisUrlFactory;

    public void send(Long actId, SendActToLisRequest request) {
        Act act = actCommandService.markSendingToLis(actId);
        UUID organizationUuid = CurrentOrganizationContext.getRequiredOrganizationUuid();

        try {
            LisResearchCode researchCode = LisResearchCode.of(act.getActType());
            Integer actTemplateId = lisActClient.resolveActTemplateId(researchCode, organizationUuid);
            String fullNameOfDoctor = currentEmployeeFullName();
            String redirectUrl = lisUrlFactory.callbackUrl(actId).toString();

            LisActPushRequest payload = payloadMapper.toPushRequest(
                    act, actTemplateId, request.priority(), request.paid(), fullNameOfDoctor, redirectUrl
            );

            Long lisActId = lisActClient.createAct(
                    request.labId(), actId, Boolean.TRUE.equals(request.force()), payload, organizationUuid
            );

            actCommandService.recordLisSendSuccess(actId, lisActId);
        } catch (LisException exception) {
            actCommandService.recordLisSendFailure(actId, exception.toShortDescription());
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Unexpected failure sending act {} to LIS", actId, exception);
            actCommandService.recordLisSendFailure(actId, "UNEXPECTED_ERROR: " + exception.getMessage());
            throw exception;
        }
    }

    private String currentEmployeeFullName() {
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        return user == null ? null : user.getFullName();
    }
}
