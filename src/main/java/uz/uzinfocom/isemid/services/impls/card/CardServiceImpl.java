package uz.uzinfocom.isemid.services.impls.card;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.enums.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCardAssignUsers;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.CardService;
import uz.uzinfocom.isemid.services.impls.card.strategy.UpdateCardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ApiResponsePage getAllCardsByFilter(int page, int size, String formTypeCode, String fromOrName, String toOrgName, CardStatus cardStatus, String fullURL) {
        Specification<Card> spec = getFilteredSpecification(formTypeCode, fromOrName, toOrgName, cardStatus);
        Page<Card> cardPage = cardRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, cardPage, fullURL);
    }

    @Override
    public ApiResponse<?> assignUsers(ReqCardAssignUsers assignUsers, User currentUser) {
        Card card = cardRepository.findById(assignUsers.getCardId()).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found."));
        Set<User> usersToAdd = assignUsers.getUserIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ApiException(NOT_FOUND, "User with ID " + userId + " not found.")))
                .collect(Collectors.toSet());
        card.getUsers().addAll(usersToAdd);
        return new ApiResponse<>(true, "successfully assigned.", cardRepository.save(card));
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "successfully retrieved.",
                cardRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found.")));
    }

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        return updateCardService.updateCard(id, reqCard, currentUser);
    }


    public static Specification<Card> getFilteredSpecification(
            String formTypeCode,
            String fromOrgName,
            String toOrgName,
            CardStatus cardStatus
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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
