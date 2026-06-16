package uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

public interface Form058StatusCountProjection {

    FormStatus getStatus();

    long getCount();
}
