package uz.uzinfocom.isemid.features.act.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.factory.ActFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.services.ActBuilderService;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActBuilderServiceImpl implements ActBuilderService {
    private final ActFactory actFactory;

    @Override
    public List<Act> buildActs(List<ActType> actTypes, Card card, User user, Map<Long, User> userMap) {
        LocalDateTime admissionDate = Optional.ofNullable(card.getForm058())
                .map(Form058::getAdmissionDate)
                .orElse(null);

        return actTypes.stream()
                .map(actType -> {
                    Act act = actFactory.createAct(actType);
                    act.setCreatedBy(user.getId());
                    act.setUpdatedBy(user.getId());
                    act.setUsers(new HashSet<>(userMap.values()));
                    act.setCardId(card.getId());
                    act.setType(actType);
                    act.setStatus(ActStatus.NEW);

                    // hospitalizationDate ni form058'dan olish
                    setHospitalizationDateIfApplicable(act, actType, admissionDate);

                    return act;
                })
                .toList();
    }

    private void setHospitalizationDateIfApplicable(Act act, ActType actType, LocalDateTime admissionDate) {
        if (admissionDate == null) return;

        switch (actType) {
            case ACT153 -> {
                if (act instanceof Act153 act153) {
                    act153.setHospitalizationDate(admissionDate);
                }
            }
            case ACT154 -> {
                if (act instanceof Act154 act154) {
                    act154.setHospitalizationDate(admissionDate);
                }
            }
            default -> {
            }
        }
    }
}