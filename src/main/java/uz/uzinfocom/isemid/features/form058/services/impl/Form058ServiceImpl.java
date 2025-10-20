package uz.uzinfocom.isemid.features.form058.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.services.CardBuildService;
import uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058AssignCardRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058ChangeDiagnosisRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058NotApproveRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058Request;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058ConfirmationStatsResponse;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.mappers.Form058Mapper;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.form058.specifications.Form058Specification;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.features.form058.util.Form058BindingUtil.bindChildEntity;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    private final Form058Repository form058Repository;
    private final CardBuildService cardBuildService;
    private final UserService userService;
    private final Form058Mapper form058Mapper;
    private final CardRepository cardRepository;
    private final Mkb10Service mkb10Service;
    private final MessageUtil messageUtil;

    @Override
    public Form058 create(Form058Request request) {
        Form058 form058 = form058Mapper.toEntity(request);
        form058.setStatus(FormStatus.SENT);
        bindChildEntity(form058);
        return form058Repository.save(form058);
    }

    @Override
    public Page<Form058> filter(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode, Boolean affiliation, User user, FilterType filterType) {
        Specification<Form058> spec = Form058Specification.build(fromDate, toDate, stateCode, cityCode, affiliation, user, filterType);
        return form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Form058 update(Form058Request request, Long id, User user) {
        Form058 existedEntity = form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        form058Mapper.update(existedEntity, request);
        return form058Repository.save(existedEntity);
    }

    @Override
    public Form058 getById(long id) {
        return form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    @Transactional
    public Form058 assignCard(Form058AssignCardRequest request, User user) {
        if (request.getAssignUserIds() == null || request.getAssignUserIds().isEmpty()) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form058.assign_user_empty"));
        }

        if (request.getCardType() == null || request.getCardType().isEmpty()) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form058.card_type_required"));
        }

        Form058 form058 = this.getById(request.getForm058Id());

        Map<Long, User> userMap = userService.getAllByIds(request.getAssignUserIds()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Card> cards = cardBuildService.buildCards(request.getCardType(), userMap, form058, user);
        form058.setCardByFullName(user.getFullName());
        form058.getCards().addAll(cards);
        form058.setStatus(FormStatus.CARD_LINKED);
        return form058Repository.save(form058);
    }

    @Override
    public Form058 changeReceivedOrd(Long formId, Long orgId) {
        Form058 form058 = this.getById(formId);
        form058.setReceiverOrganizationId(orgId);
        return form058Repository.save(form058);
    }

    @Override
    public Form058 cancel(Long formId) {
        Form058 form058 = this.getById(formId);
        form058.setStatus(FormStatus.CANCELED);
        return form058Repository.save(form058);
    }

    @Override
    public Form058 approveForm(Long formId) {
        Form058 form058 = this.getById(formId);

        if (!form058.getStatus().equals(FormStatus.CARD_LINKED)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.not_allowed_to_approve_form"));
        }
        form058.setStatus(FormStatus.APPROVED);
        return form058Repository.save(form058);
    }

    @Override
    public Form058 notApprove(Long formId, Form058NotApproveRequest notApprove) {
        Form058 form058 = this.getById(formId);
        form058.setNotApproveComment(notApprove.getComment());
        form058.setStatus(FormStatus.NOT_APPROVED);
        return form058Repository.save(form058);
    }

    @Override
    @Transactional
    public void deleteById(Long formId) {
        Form058 form058 = this.getById(formId);
        List<Card> cards = form058.getCards();

        for (Card card : cards) {
            if (card instanceof Card161 card161) {
                if (card161.getInfectionSourceDetail() != null) {
                    card161.setInfectionSourceDetail(null);
                    cardRepository.save(card161);
                }
            }
        }
        cardRepository.flush();
        try {
            form058Repository.delete(form058);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional
    public Form058 approveDiagnosis(Long formId, Form058ChangeDiagnosisRequest changeDiagnosis) {
        Form058 form058 = this.getById(formId);
        if (!form058.getStatus().equals(FormStatus.CARD_LINKED))
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form058.cannot_change_diagnosis"));
        form058.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        form058.setFinalMkb10Name(changeDiagnosis.getFinalMkb10Name());
        form058.setDiagnosisDate(LocalDateTime.now());
        form058.setStatus(FormStatus.APPROVED);
        return form058Repository.save(form058);
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
                    .mkb10Stats(mkbStats)
                    .build());
        }

        return responses;
    }

}
