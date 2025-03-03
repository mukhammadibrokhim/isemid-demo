package uz.uzinfocom.isemid.services.impls.card;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.card.CardProgressStatus;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqRejectCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.CardService;
import uz.uzinfocom.isemid.services.impls.card.strategy.UpdateCardService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    final CardRepository cardRepository;
    final UserRepository userRepository;
    final UpdateCardService updateCardService;


    @Override
    public ApiResponsePage getAllCardsByFilter(int page, int size, String formTypeCode, String fromOrName, String toOrgName, CardStatus cardStatus, User user, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(formTypeCode, fromOrName, toOrgName, cardStatus, user.getId());
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "successfully retrieved.",
                cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found.")));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        return updateCardService.updateCard(id, reqCard, currentUser);
    }

    @Override
    public ApiResponse<?> acceptCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(NOT_FOUND, "User not found."));
        if (!card.getUsers().contains(user)) {
            throw new ApiException(BAD_REQUEST, "User is not assigned to this card.");
        }

        if (card.getStatus() == CardStatus.REJECTED_BY_USER || card.getStatus() == CardStatus.REJECTED_BY_CHECKER) {
            throw new ApiException(BAD_REQUEST, "Rejected card cannot be directly accepted. It must be refilled first.");
        }

        card.setStatus(CardStatus.APPROVED);
        card.setRejectedBy(null);
        return new ApiResponse<>(true, "successfully accepted.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> rejectCard(Long cardId, ReqRejectCard rejectCard, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(NOT_FOUND, "User not found"));
        if (!card.getUsers().contains(user)) {
            throw new ApiException(BAD_REQUEST, "User is not assigned to this card.");
        }

        if (card.getProgressStatus() == CardProgressStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Completed card cannot be rejected.");
        }

        card.setStatus(CardStatus.REJECTED_BY_USER);
        card.setComment(rejectCard.getComment());
        card.setRejectedBy(user);
        return new ApiResponse<>(true, "Card successfully rejected by user.", cardRepository.save(card));
    }

    @Override
    public ApiResponsePage getCardUsers(int page, int size, Long userId, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(null, null, null, null, userId);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage, fullURL);
    }

    @Override
    public ApiResponse<?> saveAndComplete(Long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));
        if (!card.getAssignedBy().equals(currentUser)) {
            throw new ApiException(BAD_REQUEST, "User is not assigned to this card.");
        }
        if (card.getProgressStatus() == CardProgressStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Card is already completed.");
        }
        card.setProgressStatus(CardProgressStatus.COMPLETED);
        return new ApiResponse<>(true, "successfully saved.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> rejectCardByChecker(Long cardId, User user) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found."));
        if (!card.getAssignedBy().equals(user)) {
            throw new ApiException(BAD_REQUEST, "User is not assigned to this card.");
        }

        if (card.getProgressStatus() == CardProgressStatus.COMPLETED) {
            throw new ApiException(BAD_REQUEST, "Completed card cannot be rejected.");
        }

        card.setStatus(CardStatus.REJECTED_BY_CHECKER);
        card.setRejectedBy(user);
        return new ApiResponse<>(true, "successfully rejected.", cardRepository.save(card));
    }

    public static Specification<Card> getFilteredSpecification(
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus,
            Long userId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(formTypeCode)) {
                predicates.add(criteriaBuilder.equal(root.get("cardType"), formTypeCode));
            }

            if (hasText(fromOrgName)) {
                predicates.add(criteriaBuilder.equal(root.get("form058").get("senderOrganization").get("name"), fromOrgName));
            }
            if (hasText(toOrgName)) {
                predicates.add(criteriaBuilder.equal(root.get("form058").get("receiverOrganization").get("name"), toOrgName));
            }
            if (cardStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("cardStatus"), cardStatus));
            }
            if (userId != null) {
                Join<Card, User> userJoin = root.join("users");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
