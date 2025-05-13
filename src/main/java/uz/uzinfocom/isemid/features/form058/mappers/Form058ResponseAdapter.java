package uz.uzinfocom.isemid.features.form058.mappers;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;

@Component
public class Form058ResponseAdapter {
    public Form058Response adapt(Form058Response response, Form058 form, FilterType filterType) {
        if (filterType == FilterType.RECEIVED && form.getStatus() == FormStatus.SENT) {
            response.setStatus(FormStatus.NEW);
        }
        return response;
    }
}
