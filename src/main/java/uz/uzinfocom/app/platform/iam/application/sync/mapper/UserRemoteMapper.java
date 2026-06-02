package uz.uzinfocom.app.platform.iam.application.sync.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemotePractitionerPayload;

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
                .active(payload.active() == null || payload.active())
                .organizations(new LinkedHashSet<>(organizations))
                .roles(new LinkedHashSet<>(roles))
                .build();
    }
}
