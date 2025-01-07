package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Form089;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm089;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form089Repository;
import uz.uzinfocom.isemid.services.Form089Service;

@Service
@RequiredArgsConstructor
public class Form089ServiceImpl implements Form089Service {
    final Form089Repository form089Repository;

    @Override
    public ApiResponse<Form089> createForm089(ReqForm089 reqForm089, User user) {
        return new ApiResponse<>(true, "", null);
    }

    @Override
    public ApiResponse<Form089> updateForm089(int id, ReqForm089 reqForm089, User user) {
        return new ApiResponse<>(true, "", null);
    }
}
