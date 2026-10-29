package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.Role;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.payload.RemotePractitionerPayload;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class UserRemoteMapper {

    public User toEntity(
            RemotePractitionerPayload payload,
            Set<Organization> organizations,
            Set<Role> roles
    ) {
        return User.builder()
                .uuid(payload.uuid())
                .nnuzb(payload.nnuzb())
                .firstName(payload.firstName())
                .lastName(payload.lastName())
                .middleName(payload.middleName())
                .regionCode(payload.regionCode())
                .districtCode(payload.districtCode())
                .active(payload.active() == null || payload.active())
                .organizations(new LinkedHashSet<>(organizations))
                .roles(new LinkedHashSet<>(roles))
                .build();
    }
}
