package uz.uzinfocom.app.platform.scope;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.scope")
public class OrganizationScopeProperties {

    private String tashkentStateCode = "UZ-TK";
}
