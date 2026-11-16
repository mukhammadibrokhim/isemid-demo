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

    /**
     * Full name of the given user ("last first middle"), or {@code null} when
     * the id is {@code null} or unknown — used to auto-fill "who did this"
     * fields from the authenticated caller.
     */
    public String fullNameOrNull(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id)
                .map(User::getFullName)
                .filter(name -> !name.isBlank())
                .orElse(null);
    }
}
