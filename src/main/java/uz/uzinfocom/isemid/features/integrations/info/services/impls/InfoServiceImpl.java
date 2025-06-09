package uz.uzinfocom.isemid.features.integrations.info.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.integrations.info.dtos.responses.InfoIcd10Response;
import uz.uzinfocom.isemid.features.integrations.info.mappers.InfoMapper;
import uz.uzinfocom.isemid.features.integrations.info.services.InfoService;

import java.util.List;

import static uz.uzinfocom.isemid.features.integrations.gcp.util.ValidationUtil.validateByNnuzb;

@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final InfoMapper infoMapper;
    private final Form058Repository form058Repository;

    @Override
    public ApiResponse<?> getPatientMHByNnuzb(String nnuzb) {
        validateByNnuzb(nnuzb);
        List<Form058> form058s = form058Repository.findAllByNnuzbIdentifier(nnuzb);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058s);
    }

    @Override
    public ApiResponse<InfoIcd10Response> getConfirmedMkb10ByNnuzb(String nnuzb) {
        validateByNnuzb(nnuzb);
        List<Form058> form058s = form058Repository.findAllByNnuzbIdentifier(nnuzb);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, infoMapper.toIcd10Response(form058s));
    }
}
