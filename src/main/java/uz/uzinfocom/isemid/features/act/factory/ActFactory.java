package uz.uzinfocom.isemid.features.act.factory;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;

@Component
public class ActFactory {

    public Act createAct(ActType type) {
        return switch (type) {
            case ACT153 -> new Act153();
            case ACT154 -> new Act154();
            case ACT155 -> new Act155();
            case ACT156 -> new Act156();
            case ACT223 -> new Act223();
            case ACT224 -> new Act224();
        };
    }

}
