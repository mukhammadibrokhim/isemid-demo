package uz.uzinfocom.isemid.features.form0581.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.security.scope.OrganizationAccessScopeResolver;
import uz.uzinfocom.isemid.commons.security.scope.ResolvedAccessScope;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.services.CardBuildService;
import uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;
import uz.uzinfocom.isemid.features.form058.constants.Form058Sources;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058ConfirmationStatsResponse;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581ApproveDiagnosisRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581AssignCardRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581FilterRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581NotApproveRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581Request;
import uz.uzinfocom.isemid.features.form0581.mappers.Form0581Mapper;
import uz.uzinfocom.isemid.features.form0581.models.DiagnosisApproval;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.form0581.policy.Form0581Policy;
import uz.uzinfocom.isemid.features.form0581.reposiotries.Form0581Repository;
import uz.uzinfocom.isemid.features.form0581.services.Form0581Service;
import uz.uzinfocom.isemid.features.form0581.specifications.Form0581AccessSpecification;
import uz.uzinfocom.isemid.features.form0581.specifications.Form0581Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.features.form0581.util.Form0581BindingUtil.bindChildEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form0581ServiceImpl implements Form0581Service {

    private final Form0581Repository form0581Repository;
    private final CardBuildService cardBuildService;
    private final UserService userService;
    private final Form0581Mapper form0581Mapper;
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;
    private final Form0581Policy policy;
    private final Form0581AccessSpecification form0581AccessSpecification;
    private final OrganizationAccessScopeResolver organizationAccessScopeResolver;
    private final Mkb10Service mkb10Service;
    private final CatalogRepository catalogRepository;
    private final OrganizationService organizationService;

    @Override
    @Transactional
    public Form0581 create(Form0581Request request, String sourceHeader, User currentUser) {
        Form0581 form0581 = form0581Mapper.toEntity(request);

        Organization currentOrganization = currentUser.getCurrentOrganization();
        policy.validateCreatPermission(currentOrganization);
        form0581.setStatus(FormStatus.SENT);
        form0581.setSource(resolveSource(currentUser, sourceHeader));

        bindChildEntity(form0581);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.create.success formId={} actorUserId={} status={} source={}",
                saved.getId(), currentUser.getId(), saved.getStatus(), saved.getSource());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form0581> getAll(Form0581FilterRequest filterRequest, User user, FilterType filterType) {
        Organization currentOrganization = user.getCurrentOrganization();
        ResolvedAccessScope scope = organizationAccessScopeResolver.resolve(currentOrganization);

        FilterType effectiveFilterType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form0581> specification = Specification
                .where(Form0581Specification.build(filterRequest, effectiveFilterType))
                .and(form0581AccessSpecification.byScope(
                        effectiveFilterType,
                        scope,
                        currentOrganization == null ? null : currentOrganization.getId(),
                        filterRequest == null ? null : filterRequest.affiliation()
                ));

        assert filterRequest != null;
        return form0581Repository.findAll(
                specification,
                PageRequest.of(filterRequest.page() - 1, filterRequest.size(), Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form0581> getByPatientNnuzb(int page, int size, String nnuzb) {
        Specification<Form0581> spec = Form0581Specification.build(nnuzb);
        return form0581Repository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    @Transactional
    public Form0581 update(Form0581Request request, Long id, User user) {
        Form0581 existedEntity = form0581Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        policy.assertEditable(existedEntity);

        FormStatus oldStatus = existedEntity.getStatus();

        form0581Mapper.update(existedEntity, request);

        Form0581 saved = form0581Repository.save(existedEntity);

        log.info("form0581.update.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Form0581 getById(long id) {
        return form0581Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    @Transactional
    public Form0581 assignCard(Form0581AssignCardRequest request, User user) {
        Form0581 form0581 = getById(request.getForm0581Id());

        policy.assertCardAssignable(form0581, request.getCardType());

        List<Long> userIds = request.getAssignUserIds().stream().filter(Objects::nonNull).distinct().toList();

        Map<Long, User> userMap = userService.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        if (userMap.size() != userIds.size()) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form0581.assign_user_not_found"));
        }

        FormStatus oldStatus = form0581.getStatus();

        List<Card> cards = cardBuildService.buildCards(request.getCardType(), userMap, form0581, user);

        form0581.setReceiverFullName(user.getFullName());
        form0581.getCards().addAll(cards);
        form0581.setStatus(FormStatus.CARD_LINKED);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.assignCard.success formId={} actorUserId={} oldStatus={} newStatus={} assigneesCount={} cardsCreated={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus(), userIds.size(), cards.size());

        return saved;
    }

    @Override
    @Transactional
    public Form0581 changeReceivedOrg(Long formId, Long orgId) {
        Form0581 form0581 = getById(formId);
        Long previousReceiverOrgId = form0581.getReceiverOrganizationId();
        form0581.setReceiverOrganizationId(orgId);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.changeReceiver.success formId={} oldReceiverOrganizationId={} newReceiverOrganizationId={}",
                saved.getId(), previousReceiverOrgId, saved.getReceiverOrganizationId());

        return saved;
    }

    @Override
    @Transactional
    public Form0581 cancel(Long formId, String reason, User currentUser) {
        Form0581 form0581 = getById(formId);

        policy.assertCancelable(form0581);

        FormStatus oldStatus = form0581.getStatus();

        form0581.setStatus(FormStatus.CANCELED);
        form0581.setCancelReason(reason.trim());
        form0581.setCanceledBy(currentUser.getId());

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.cancel.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public Form0581 approveForm(Long formId, User currentUser) {
        Form0581 form0581 = getById(formId);

        policy.assertApprovable(form0581);

        FormStatus oldStatus = form0581.getStatus();

        form0581.setStatus(FormStatus.APPROVED);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.approveForm.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public Form0581 notApprove(Long formId, User user, Form0581NotApproveRequest notApprove) {
        Form0581 form0581 = getById(formId);

        policy.assertNotApprovable(form0581);

        FormStatus oldStatus = form0581.getStatus();

        DiagnosisApproval diagnosisApproval = form0581.getDiagnosisApproval();
        if (diagnosisApproval == null) {
            diagnosisApproval = new DiagnosisApproval();
            form0581.setDiagnosisApproval(diagnosisApproval);
        }
        diagnosisApproval.setNotApproveComment(notApprove.getComment().trim());
        form0581.setStatus(FormStatus.NOT_APPROVED);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.notApprove.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public Form0581 approveDiagnosis(Long formId, Form0581ApproveDiagnosisRequest changeDiagnosis, String token,
                                     User currentUser) {
        Form0581 form0581 = getById(formId);

        policy.assertDiagnosisApprovable(form0581);

        FormStatus oldStatus = form0581.getStatus();

        DiagnosisApproval diagnosisApproval = form0581.getDiagnosisApproval();
        if (diagnosisApproval == null) {
            diagnosisApproval = new DiagnosisApproval();
            form0581.setDiagnosisApproval(diagnosisApproval);
        }
        diagnosisApproval.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        diagnosisApproval.setFinalMkb10Name(changeDiagnosis.getFinalMkb10Name());
        diagnosisApproval.setApprovedFullName(changeDiagnosis.getApprovedFullName());

        if (changeDiagnosis.getApprovedOrgId() != null) {
            Organization approveOrg = organizationService
                    .findById(changeDiagnosis.getApprovedOrgId())
                    .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(
                            changeDiagnosis.getApprovedOrgId(), token
                    ));

            diagnosisApproval.setApprovedOrgUuid(approveOrg.getUuid());
        } else {
            diagnosisApproval.setApprovedOrgUuid(null);
        }

        form0581.setStatus(FormStatus.APPROVED);

        Form0581 saved = form0581Repository.save(form0581);

        log.info("form0581.approveDiagnosis.success formId={} actorUserId={} oldStatus={} newStatus={} finalMkb10Code={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus(), diagnosisApproval.getFinalMkb10Code());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Form058ConfirmationStatsResponse> getConfirmationStats(
            List<String> mkb10Codes,
            List<String> regionCodes,
            LocalDate fromDate,
            LocalDate toDate,
            String locale,
            User currentUser
    ) {
        List<String> regions = (regionCodes == null || regionCodes.isEmpty())
                ? List.of(currentUser.getCurrentOrganization().getStateCode())
                : regionCodes;

        List<Mkb10Projection> mkb10List = mkb10Service.getNamesByCodesAndLocale(mkb10Codes, locale);

        List<Form058ConfirmationStatsResponse> responses = new ArrayList<>();

        for (String region : regions) {
            String regionName = catalogRepository.findRegionNameByCodeAndLocale(region, locale).orElse(null);
            List<Form058ConfirmationStatsResponse.Form058Mkb10Stat> mkbStats = new ArrayList<>();

            for (Mkb10Projection mkb : mkb10List) {
                String mkbCode = mkb.getCode();
                String mkbName = mkb.getName() != null ? mkb.getName() : "Unknown ICD-10";

                Specification<Form0581> confirmedSpec =
                        Form0581Specification.buildStats(List.of(region), fromDate, toDate, List.of(mkbCode), true);

                Specification<Form0581> unconfirmedSpec =
                        Form0581Specification.buildStats(List.of(region), fromDate, toDate, List.of(mkbCode), false);

                long confirmed = form0581Repository.count(confirmedSpec);
                long unconfirmed = form0581Repository.count(unconfirmedSpec);

                mkbStats.add(Form058ConfirmationStatsResponse.Form058Mkb10Stat.builder()
                        .code(mkbCode)
                        .name(mkbName)
                        .confirmed(confirmed)
                        .unconfirmed(unconfirmed)
                        .build());
            }

            responses.add(Form058ConfirmationStatsResponse.builder()
                    .regionCode(region)
                    .regionName(regionName)
                    .mkb10Stats(mkbStats)
                    .build());
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteById(Long formId) {
        Form0581 form0581 = getById(formId);

        policy.assertDeletable(form0581);

        int cardsCount = form0581.getCards().size();

        try {
            form0581Repository.delete(form0581);
            form0581Repository.flush();

            log.info("form0581.delete.success formId={} cardsCount={}", formId, cardsCount);

        } catch (Exception e) {
            log.error("form0581.delete.fail formId={} reason={}", formId, e.getMessage(), e);
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Form0581 getByCardId(Long cardId) {
        return cardRepository.findForm0581ByCardId(cardId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, messageUtil.get("form0581.not_found_by_card_id")));
    }

    private String resolveSource(User currentUser, String sourceHeader) {
        if (currentUser.hasRole("isemid_qr_user")) {
            return Form058Sources.QR;
        }

        if (sourceHeader != null && !sourceHeader.isBlank()) {
            String normalized = sourceHeader.trim().toUpperCase();

            if (Form058Sources.LOCAL.equals(normalized)) {
                return Form058Sources.LOCAL;
            }
        }
        return Form058Sources.DMED;
    }
}
