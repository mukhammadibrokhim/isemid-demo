package uz.uzinfocom.isemid.features.form129.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129FilterRequest;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129Request;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.user.models.User;

public interface Form129Service {

    Form129 create(Form129Request request, User currentUser);

    Page<Form129> getAll(Form129FilterRequest filterRequest, User user, FilterType filterType);

    Form129 getById(long id);

    Form129 update(Form129Request request, Long id, User user);

    void deleteById(Long formId);
}
