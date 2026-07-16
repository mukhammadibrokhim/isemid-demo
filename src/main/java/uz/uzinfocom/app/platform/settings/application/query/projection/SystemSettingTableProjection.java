package uz.uzinfocom.app.platform.settings.application.query.projection;

import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;

public interface SystemSettingTableProjection {

    Long getId();

    String getSettingKey();

    String getSettingValue();

    SystemSettingValueType getValueType();

    Boolean getActive();
}
