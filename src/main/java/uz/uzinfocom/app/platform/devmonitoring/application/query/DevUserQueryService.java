package uz.uzinfocom.app.platform.devmonitoring.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevUserResponse;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevUserRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevUserQueryService {

    private final DevUserRepository devUserRepository;

    @Transactional(readOnly = true)
    public List<DevUserResponse> findAll() {
        return devUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DevUserResponse getById(Long id) {
        DevUser devUser = devUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-user.not-found", id));

        return toResponse(devUser);
    }

    private DevUserResponse toResponse(DevUser devUser) {
        return new DevUserResponse(
                devUser.getId(),
                devUser.getUsername(),
                devUser.isEnabled(),
                devUser.isRoot(),
                devUser.getCreatedAt()
        );
    }
}
