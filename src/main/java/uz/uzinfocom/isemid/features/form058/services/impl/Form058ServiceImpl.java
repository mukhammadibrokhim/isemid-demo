package uz.uzinfocom.isemid.features.form058.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.factory.CardFactory;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058DetailsResponse;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.mappers.Form058Mapper;
import uz.uzinfocom.isemid.features.form058.mappers.Form058ResponseAdapter;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.form058.specifications.Form058Specification;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    private final Form058Repository form058Repository;
    private final CardFactory cardFactory;
    private final UserService userService;
    private final Form058Mapper form058Mapper;
    private final Form058ResponseAdapter form058ResponseAdapter;
    private final CardRepository cardRepository;
    private final MessageUtil messageUtil;

    @Override
    public ApiResponse<Form058Response> create(Form058Request request, User user, String token) {
        Form058 form058 = form058Mapper.toEntity(request);
        form058.setStatus(FormStatus.SENT);
        Patient patient = form058.getPatient();
        if (patient != null && patient.getIdentifiers() != null) {
            patient.getIdentifiers().forEach(identifier -> identifier.setPatient(patient));
            patient.getAddresses().forEach(address -> address.setPatient(patient));
            if (patient.getAffiliations() != null) {
                patient.getAffiliations().forEach(affiliation -> affiliation.setPatient(patient));
            }
        }

        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> update(Form058Request request, Long id, User user) {
        Form058 existedForm058 = form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        form058Mapper.update(existedForm058, request);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(existedForm058)));
    }

    @Override
    public PagedResponse<?> getAllFilteredForm058(int page, int size, LocalDate fromDate, LocalDate toDate, User user, String url, FilterType filterType) {
        Specification<Form058> spec = Form058Specification.build(fromDate, toDate, user, filterType);
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        Page<Form058DetailsResponse> responsePage = form058Page.map(form -> {
            Form058DetailsResponse base = form058Mapper.toDetailsResponse(form);
            return form058ResponseAdapter.adapt(base, form, filterType);
        });
        return PagedResponseUtil.build(page, responsePage, url, messageUtil);
    }

    private Form058 getForm058ById(Long id) {
        return form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public ApiResponse<Form058Response> getById(long id) {
        return ApiResponse.success(messageUtil.get("message.success"),
                form058Mapper.toResponse(form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)))));
    }

    @Override
    public ApiResponse<Form058Response> assignCard(Form058AssignCardRequest request, User user) {
        if (request.getAssignUserIds() == null || request.getAssignUserIds().isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Список назначенных пользователей пуст.");
        }

        if (request.getCardType() == null || request.getCardType().isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Требуются типы карт.");
        }
        Form058 form058 = this.getForm058ById(request.getForm058Id());

        Map<Long, User> userMap = userService.getAllByIds(request.getAssignUserIds()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Card> cards = buildCards(request.getCardType(), userMap, form058, user);
        form058.setCardByFullName(user.getFullName());
        form058.getCards().addAll(cards);
        form058.setStatus(FormStatus.CARD_LINKED);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    private List<Card> buildCards(List<CardType> cardTypes, Map<Long, User> userMap, Form058 form058, User assignedBy) {
        List<Card> resultCards = new ArrayList<>();

        for (CardType cardType : cardTypes) {
            Optional<Card> existingCardOpt = form058.getCards().stream()
                    .filter(card -> card.getCardType() == cardType)
                    .findFirst();

            if (existingCardOpt.isPresent()) {
                Card existingCard = existingCardOpt.get();
                existingCard.setUsers(new HashSet<>(userMap.values()));
                existingCard.setStatus(CardStatus.NEW);
                resultCards.add(existingCard);
            } else {
                Card newCard = cardFactory.createCard(cardType);
                newCard.setCreatedBy(assignedBy.getId());
                newCard.setUpdatedBy(assignedBy.getId());
                newCard.setUsers(new HashSet<>(userMap.values()));
                newCard.setStatus(CardStatus.NEW);
                newCard.setCardType(cardType);
                newCard.setAssignedById(assignedBy.getId());
                newCard.setForm058(form058);
                resultCards.add(newCard);
            }
        }

        return resultCards;
    }

    @Override
    public ApiResponse<Form058Response> changeReceivedOrd(Long formId, Form058UpdateReceivedOrg updateReceivedOrg) {
        Form058 form058 = this.getForm058ById(formId);
        form058.setReceiverOrganizationId(updateReceivedOrg.getReceivedOrgId());
        form058.getPatient().getAddresses().stream()
                .filter(address -> address.getType().equals(AddressType.TEMPORARY))
                .findFirst()
                .ifPresent(address -> {
                    address.setStateCode(updateReceivedOrg.getAddressStateCode());
                    address.setCityCode(updateReceivedOrg.getAddressCityCode());
                    address.setNeighborhoodCode(updateReceivedOrg.getAddressNeighborhoodCode());
                    address.setStreetAddress(updateReceivedOrg.getStreetAddress());
                    address.setHouseNumber(updateReceivedOrg.getHouseNumber());
                    address.setApartmentNumber(updateReceivedOrg.getApartmentNumber());
                });
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> cancel(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
        form058.setStatus(FormStatus.CANCELED);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> approveForm(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
        boolean allCardsApproved = form058.getCards().stream()
                .allMatch(card -> card.getStatus().equals(CardStatus.APPROVED));
        if (!allCardsApproved) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.card.not_approved"));
        }
        form058.setStatus(FormStatus.APPROVED);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> notApprove(Long formId, Form058NotApproveRequest notApprove) {
        Form058 form058 = this.getForm058ById(formId);
        form058.setNotApproveComment(notApprove.getComment());
        form058.setStatus(FormStatus.NOT_APPROVED);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<?> deleteById(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
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
        return ApiResponse.success(messageUtil.get("message.success"));
    }

    @Override
    public ApiResponse<?> changeDiagnosis(Long formId, Form058ChangeDiagnosisRequest changeDiagnosis) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", formId)));
        if (!form058.getStatus().equals(FormStatus.NOT_APPROVED))
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.form058.cannot_change_diagnosis"));
        form058.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        form058.setFinalMkb10Name(changeDiagnosis.getFinalMkb10Name());
        form058.setDiagnosisDate(LocalDateTime.now());
        form058.setStatus(FormStatus.APPROVED);
        return ApiResponse.success(messageUtil.get("message.success"), form058Mapper.toResponse(form058Repository.save(form058)));
    }
}
