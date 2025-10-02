package uz.uzinfocom.isemid.features.act.services.impls;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.factory.ActFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.services.ActBuildService;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
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

                    if (actType == ActType.ACT153 || actType == ActType.ACT154) {
                        Form058 form058 = card.getForm058();
                        if (form058 != null && form058.getAdmissionDate() != null) {
                            if (act instanceof Act153 act153) {
                                act153.setHospitalizationDate(form058.getAdmissionDate());
                            } else if (act instanceof Act154 act154) {
                                act154.setHospitalizationDate(form058.getAdmissionDate());
                            }
                        }
                    }

                    return act;
                })
                .collect(Collectors.toList());
    }

}
