package uz.uzinfocom.app.modules.form0581.application.query.mapper.helper;

import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableStatus;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.persistence.mapper.TableStatusMapper;

@Component
public class Form0581TableMapperHelper {

    @Named("toTableStatus")
    public Form0581TableStatus toTableStatus(
            Form0581TableProjection projection,
            @Context Form0581Direction direction
    ) {
        if (projection == null) {
            return null;
        }

        Form0581Status status = projection.getStatus();

        return TableStatusMapper.deriveTableStatus(
                status,
                direction == Form0581Direction.INCOMING && status == Form0581Status.SENT,
                Form0581TableStatus.NEW,
                Form0581TableStatus.class
        );
    }
}
