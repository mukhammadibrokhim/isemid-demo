package uz.uzinfocom.isemid.features.integrations.dmed;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import uz.uzinfocom.isemid.features.integrations.dmed.dto.Form058ChangeEvent;

@HttpExchange(contentType = "application/json")
public interface DmedForm0581Client {

    @PatchExchange
    void pushFormStatusChangeEvent(@PathVariable("formId") Long formId,
        @RequestBody Form058ChangeEvent formStatus);
}
