package uz.uzinfocom.app.modules.iam.application.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.modules.iam.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    public List<User> getAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(ids);
    }
}
