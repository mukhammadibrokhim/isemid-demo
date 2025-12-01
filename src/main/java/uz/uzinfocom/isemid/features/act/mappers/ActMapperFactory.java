package uz.uzinfocom.isemid.features.act.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.mappers.act153.Act153Mapper;
import uz.uzinfocom.isemid.features.act.mappers.act154.Act154Mapper;
import uz.uzinfocom.isemid.features.act.mappers.act155.Act155Mapper;
import uz.uzinfocom.isemid.features.act.mappers.act156.Act156Mapper;
import uz.uzinfocom.isemid.features.act.mappers.act223.Act223Mapper;
import uz.uzinfocom.isemid.features.act.mappers.act224.Act224Mapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;

@Component
@RequiredArgsConstructor
public class ActMapperFactory {
    private final Act153Mapper act153Mapper;
    private final Act154Mapper act154Mapper;
    private final Act155Mapper act155Mapper;
    private final Act156Mapper act156Mapper;
    private final Act223Mapper act223Mapper;
    private final Act224Mapper act224Mapper;

    public ActResponse toResponse(Act act) {
        return switch (act.getType()) {
            case ACT153 -> act153Mapper.toResponse((Act153) act);
            case ACT154 -> act154Mapper.toResponse((Act154) act);
            case ACT155 -> act155Mapper.toResponse((Act155) act);
            case ACT156 -> act156Mapper.toResponse((Act156) act);
            case ACT223 -> act223Mapper.toResponse((Act223) act);
            case ACT224 -> act224Mapper.toResponse((Act224) act);
        };
    }

    public void update(Act act, ActRequest requestDto) {
        switch (act.getType()) {
            case ACT153 -> act153Mapper.update((Act153) act, (Act153Request) requestDto);
            case ACT154 -> act154Mapper.update((Act154) act, (Act154Request) requestDto);
            case ACT155 -> act155Mapper.update((Act155) act, (Act155Request) requestDto);
            case ACT156 -> act156Mapper.update((Act156) act, (Act156Request) requestDto);
            case ACT223 -> act223Mapper.update((Act223) act, (Act223Request) requestDto);
            case ACT224 -> act224Mapper.update((Act224) act, (Act224Request) requestDto);
        }
    }
}
