package uz.uzinfocom.isemid.features.act.services;

import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Map;

public interface ActBuilderService {
    List<Act> buildActs(List<ActType> actTypes, Card card, User user, Map<Long, User> userMap);
}
