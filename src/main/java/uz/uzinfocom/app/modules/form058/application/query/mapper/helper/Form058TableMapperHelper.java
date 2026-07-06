package uz.uzinfocom.app.modules.form058.application.query.mapper.helper;

import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableStatus;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;

@Component
public class Form058TableMapperHelper {

    @Named("toTableStatus")
    public Form058TableStatus toTableStatus(
            Form058TableProjection projection,
            @Context Form058Direction direction
    ) {
        if (projection == null || projection.getStatus() == null) {
            return null;
        }

        FormStatus status = projection.getStatus();

        if (direction == Form058Direction.INCOMING && status == FormStatus.SENT) {
            return Form058TableStatus.NEW;
        }

        return Form058TableStatus.valueOf(status.name());
    }
}
