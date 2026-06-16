package uz.uzinfocom.app.modules.form058.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.shared.OrganizationIdResolver;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.Location;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058Repository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateForm058Service {

    private final Form058Repository form058Repository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final Form058UpdateValidator form058UpdateValidator;
    private final OrganizationIdResolver organizationIdResolver;

    @Transactional
    public UpdateForm058Result update(UpdateForm058Command command) {
        Form058 form058 = findRequired(command.id());
        form058UpdateValidator.validate(form058, command);
        ensureLocation(form058);
        form058UpdateMapper.update(command, form058);
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }

    @Transactional
    public UpdateForm058Result changeReceiver(Long formId, UUID receiverOrganizationUuid) {
        Form058 form058 = findRequired(formId);
        Long receiverOrganizationId = organizationIdResolver.resolveActiveId(receiverOrganizationUuid);
        UpdateForm058Command command = new UpdateForm058Command(
                formId,
                null,
                null,
                null,
                null,
                null,
                null,
                receiverOrganizationId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        form058UpdateValidator.validate(form058, command);
        form058.setReceiverOrganizationId(receiverOrganizationId);
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }

    @Transactional
    public UpdateForm058Result assignCard(Long formId, Long cardId) {
        Form058 form058 = findRequired(formId);
        validateVisibleScope(form058);
        form058.assignCard(cardId);
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }

    private Form058 findRequired(Long id) {
        return form058Repository.findById(id)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }

    private void ensureLocation(Form058 form058) {
        if (form058.getLocation() == null) {
            form058.setLocation(new Location());
        }
    }

    private void validateVisibleScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);
        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())
                && !Objects.equals(currentOrganizationId, form058.getReceiverOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
