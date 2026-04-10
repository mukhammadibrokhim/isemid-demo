package uz.uzinfocom.isemid.features.form058.services.impl;

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
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.services.CardBuildService;
import uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;
import uz.uzinfocom.isemid.features.form058.constants.Form058Sources;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058ConfirmationStatsResponse;
import uz.uzinfocom.isemid.features.form058.mappers.Form058Mapper;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.policy.Form058Policy;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.form058.specifications.Form058AccessSpecification;
import uz.uzinfocom.isemid.features.form058.specifications.Form058Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.features.form058.util.Form058BindingUtil.bindChildEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {

    private final Form058Repository form058Repository;
    private final CardBuildService cardBuildService;
    private final UserService userService;
    private final Form058Mapper form058Mapper;
    private final CardRepository cardRepository;
    private final Mkb10Service mkb10Service;
    private final CatalogRepository catalogRepository;
    private final MessageUtil messageUtil;
    private final OrganizationService organizationService;
    private final Form058Policy policy;
    private final Form058AccessSpecification form058AccessSpecification;
    private final OrganizationAccessScopeResolver organizationAccessScopeResolver;

    @Override
    @Transactional
    public Form058 create(Form058Request request, String sourceHeader, User currentUser) {
        Form058 form058 = form058Mapper.toEntity(request);

        form058.setStatus(FormStatus.SENT);
        form058.setSource(resolveSource(currentUser, sourceHeader));

        bindChildEntity(form058);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.create.success formId={} actorUserId={} status={} source={}",
                saved.getId(), currentUser.getId(), saved.getStatus(), saved.getSource());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form058> getAll(Form058FilterRequest filterRequest, User user, FilterType filterType) {
        Organization currentOrganization = user.getCurrentOrganization();
        ResolvedAccessScope scope = organizationAccessScopeResolver.resolve(currentOrganization);

        FilterType effectiveFilterType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form058> specification = Specification
                .where(Form058Specification.build(filterRequest))
                .and(form058AccessSpecification.byScope(
                        effectiveFilterType,
                        scope,
                        currentOrganization == null ? null : currentOrganization.getId(),
                        filterRequest == null ? null : filterRequest.affiliation()
                ));

        assert filterRequest != null;
        return form058Repository.findAll(
                specification,
                PageRequest.of(filterRequest.page() - 1, filterRequest.size(), Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public Page<Form058> filter(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                String cityCode, Boolean affiliation, String mkb10Code, Long organizationId,
                                String patientDocValue, User user, FilterType filterType) {
        Specification<Form058> spec = Form058Specification.build(
                fromDate, toDate, stateCode, cityCode, affiliation, mkb10Code, organizationId, patientDocValue, user, filterType);
        return form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form058> getByPatientNnuzb(int page, int size, String nnuzb) {
        Specification<Form058> spec = Form058Specification.build(nnuzb);
        return form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    @Transactional
    public Form058 update(Form058Request request, Long id, User user) {
        Form058 existedEntity = form058Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        policy.assertEditable(existedEntity);

        FormStatus oldStatus = existedEntity.getStatus();

        form058Mapper.update(existedEntity, request);

        Form058 saved = form058Repository.save(existedEntity);

        log.info("form058.update.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Form058 getById(long id) {
        return form058Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    @Transactional
    public Form058 assignCard(Form058AssignCardRequest request, User user) {
        Form058 form058 = getById(request.getForm058Id());

        policy.assertCardAssignable(form058);

        List<Long> userIds = request.getAssignUserIds().stream().filter(Objects::nonNull).distinct().toList();

        Map<Long, User> userMap = userService.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        if (userMap.size() != userIds.size()) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form058.assign_user_not_found"));
        }

        FormStatus oldStatus = form058.getStatus();

        List<Card> cards = cardBuildService.buildCards(request.getCardType(), userMap, form058, user);

        form058.setCardByFullName(user.getFullName());
        form058.getCards().addAll(cards);
        form058.setStatus(FormStatus.CARD_LINKED);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.assignCard.success formId={} actorUserId={} oldStatus={} newStatus={} assigneesCount={} cardsCreated={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus(), userIds.size(), cards.size());

        return saved;
    }

    @Override
    @Transactional
    public Form058 changeReceivedOrd(Long formId, Long orgId) {
        Form058 form058 = getById(formId);
        Long receivedOrgId = form058.getReceiverOrganizationId();
        form058.setReceiverOrganizationId(orgId);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.changeReceiver.success formId={} oldReceiverOrganization{} new ReceiverOrganizationId={}",
                saved.getId(), receivedOrgId, saved.getReceiverOrganizationId());

        return saved;
    }

    @Override
    @Transactional
    public Form058 cancel(Long formId, String reason, User currentUser) {

        Form058 form058 = getById(formId);

        policy.assertCancelable(form058);

        FormStatus oldStatus = form058.getStatus();

        form058.setStatus(FormStatus.CANCELED);
        form058.setCancelReason(reason.trim());
        form058.setCanceledBy(currentUser.getId());

        Form058 saved = form058Repository.save(form058);

        log.info("form058.cancel.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public Form058 approveForm(Long formId, User currentUser) {
        Form058 form058 = getById(formId);

        policy.assertApprovable(form058);

        FormStatus oldStatus = form058.getStatus();

        form058.setStatus(FormStatus.APPROVED);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.approveForm.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public Form058 notApprove(Long formId, User user, Form058NotApproveRequest notApprove) {
        Form058 form058 = getById(formId);

        policy.assertNotApprovable(form058);

        FormStatus oldStatus = form058.getStatus();

        form058.setNotApproveComment(notApprove.getComment().trim());
        form058.setStatus(FormStatus.NOT_APPROVED);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.notApprove.success formId={} actorUserId={} oldStatus={} newStatus={}",
                saved.getId(), user.getId(), oldStatus, saved.getStatus());

        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long formId) {
        Form058 form058 = getById(formId);

        policy.assertDeletable(form058);

        List<Card> cards = form058.getCards();

        for (Card card : cards) {
            if (card instanceof Card161 card161) {
                if (card161.getInfectionSourceDetail() != null) {
                    card161.setInfectionSourceDetail(null);
                }
            }
        }

        try {
            form058Repository.delete(form058);
            form058Repository.flush();

            log.info("form058.delete.success formId={} cardsCount={}", formId, cards.size());

        } catch (Exception e) {
            log.error("form058.delete.fail formId={} reason={}", formId, e.getMessage(), e);
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional
    public Form058 approveDiagnosis(Long formId, Form058ApproveDiagnosisRequest changeDiagnosis, String token,
                                    User currentUser) {
        Form058 form058 = getById(formId);

        policy.assertDiagnosisApprovable(form058);

        FormStatus oldStatus = form058.getStatus();

        form058.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        form058.setFinalMkb10Name(changeDiagnosis.getFinalMkb10Name());
        form058.setApprovedFullName(changeDiagnosis.getApprovedFullName());

        if (changeDiagnosis.getApprovedOrgId() != null) {
            Organization approveOrg = organizationService
                    .findById(changeDiagnosis.getApprovedOrgId())
                    .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(
                            changeDiagnosis.getApprovedOrgId(), token
                    ));

            form058.setApprovedOrgUuid(approveOrg.getUuid());
        } else {
            form058.setApprovedOrgUuid(null);
        }

        form058.setDiagnosisDate(LocalDateTime.now());
        form058.setStatus(FormStatus.APPROVED);

        Form058 saved = form058Repository.save(form058);

        log.info("form058.approveDiagnosis.success formId={} actorUserId={} oldStatus={} newStatus={} finalMkb10Code={}",
                saved.getId(), currentUser.getId(), oldStatus, saved.getStatus(), saved.getFinalMkb10Code());

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

                Specification<Form058> confirmedSpec =
                        Form058Specification.buildStats(List.of(region), fromDate, toDate, List.of(mkbCode), true);

                Specification<Form058> unconfirmedSpec =
                        Form058Specification.buildStats(List.of(region), fromDate, toDate, List.of(mkbCode), false);

                long confirmed = form058Repository.count(confirmedSpec);
                long unconfirmed = form058Repository.count(unconfirmedSpec);

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
    @Transactional(readOnly = true)
    public Form058 getByCardId(Long cardId) {
        return cardRepository.findForm058ByCardId(cardId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, messageUtil.get("form058.not_found_by_card_id")));
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
            if (Form058Sources.HEPATITIS.equals(normalized)) {
                return Form058Sources.HEPATITIS;
            }
        }
        return Form058Sources.DMED;
    }

}
