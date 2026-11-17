package uz.uzinfocom.app.platform.devpanel.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevPositionCreateRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevPositionUpdateRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.DevPositionQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionResponse;
import uz.uzinfocom.app.platform.devpanel.domain.DevPosition;
import uz.uzinfocom.app.platform.devpanel.repository.DevPositionRepository;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;

@Service
@RequiredArgsConstructor
public class DevPositionCommandService {

    private final DevPositionRepository devPositionRepository;
    private final DevUserRepository devUserRepository;
    private final DevPositionQueryService devPositionQueryService;

    @Transactional
    public DevPositionResponse create(DevPositionCreateRequest request) {
        String name = request.name().trim();
        if (devPositionRepository.existsByName(name)) {
            throw new ConflictException("dev-position.name.already-exists", name);
        }

        DevPosition saved = devPositionRepository.save(
                DevPosition.builder().name(name).enabled(true).build()
        );

        return devPositionQueryService.getById(saved.getId());
    }

    @Transactional
    public DevPositionResponse update(Long id, DevPositionUpdateRequest request) {
        DevPosition devPosition = devPositionQueryService.findEntity(id);

        String name = request.name().trim();
        if (!name.equalsIgnoreCase(devPosition.getName()) && devPositionRepository.existsByName(name)) {
            throw new ConflictException("dev-position.name.already-exists", name);
        }

        devPosition.setName(name);
        devPosition.setEnabled(request.enabled());
        devPositionRepository.save(devPosition);

        return devPositionQueryService.getById(id);
    }

    @Transactional
    public void delete(Long id) {
        DevPosition devPosition = devPositionQueryService.findEntity(id);

        if (devUserRepository.existsByPositionId(id)) {
            throw new ConflictException("dev-position.delete.in-use", devPosition.getName());
        }

        devPositionRepository.delete(devPosition);
    }
}
