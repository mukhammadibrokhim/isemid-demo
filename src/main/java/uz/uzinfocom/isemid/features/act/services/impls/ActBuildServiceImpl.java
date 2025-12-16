package uz.uzinfocom.isemid.features.act.services.impls;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.factory.ActFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.services.ActBuildService;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActBuildServiceImpl implements ActBuildService {
    private final ActFactory factory;

    /**
     * Creates a list of new Act entities for the given Card and user assignments.
     *
     * @param actTypes list of ActType to create
     * @param card     card to which acts belong
     * @param user     current user performing assignment
     * @param userMap  users assigned to each act
     * @return list of new Act objects
     */
    @Override
    public List<Act> actBuilder(List<ActType> actTypes, Card card, User user, Map<Long, User> userMap) {
        return actTypes.stream()
                .map(actType -> {
                    Act act = factory.createAct(actType);
                    act.setCreatedBy(user.getId());
                    act.setUpdatedBy(user.getId());
                    act.setUsers(new HashSet<>(userMap.values()));
                    act.setCardId(card.getId());
                    act.setType(actType);
                    act.setStatus(ActStatus.NEW);
                    return act;
                })
                .collect(Collectors.toList());
    }

}
