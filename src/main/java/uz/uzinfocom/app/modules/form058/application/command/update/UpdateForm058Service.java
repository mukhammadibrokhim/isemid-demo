package uz.uzinfocom.app.modules.form058.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.service.PatientIdentifierSync;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditFieldDiff;
import uz.uzinfocom.app.platform.audit.event.FieldsChangedEvent;
import uz.uzinfocom.app.platform.audit.event.OrganizationReassignedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final Form058UpdateValidator form058UpdateValidator;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm058Result update(UpdateForm058Command command) {
        Form058 form058 = findRequired(command.id());
        form058UpdateValidator.validate(form058, command);
        Long oldReceiverOrganizationId = form058.getReceiverOrganizationId();
        Map<String, Object> before = form058.auditFields();
        form058UpdateMapper.update(command, form058);
        updatePatient(command, form058.getPatient());
        UpdateForm058Result result = form058UpdateMapper.toResult(form058Repository.save(form058));

        Long actorUserId = currentUserProvider.userIdOrNull();
        Long newReceiverOrganizationId = form058.getReceiverOrganizationId();
        if (!Objects.equals(oldReceiverOrganizationId, newReceiverOrganizationId)) {
            eventPublisher.publishEvent(new OrganizationReassignedEvent(
                    AuditEntityType.FORM058, form058.getId(),
                    oldReceiverOrganizationId, newReceiverOrganizationId,
                    actorUserId
            ));
        }

        Map<String, Object> changes = AuditFieldDiff.compute(before, form058.auditFields());
        if (!changes.isEmpty()) {
            eventPublisher.publishEvent(new FieldsChangedEvent(
                    AuditEntityType.FORM058, form058.getId(), changes, actorUserId
            ));
        }

        return result;
    }

    private Form058 findRequired(Long id) {
        return form058Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }

    private void updatePatient(UpdateForm058Command command, Patient patient) {
        UpdatePatientCommand patientCommand = command.patient();
        if (patient == null || patientCommand == null) {
            return;
        }

        if (patientCommand.firstName() != null) {
            patient.setFirstName(patientCommand.firstName());
        }
        if (patientCommand.lastName() != null) {
            patient.setLastName(patientCommand.lastName());
        }
        if (patientCommand.middleName() != null) {
            patient.setMiddleName(patientCommand.middleName());
        }
        if (patientCommand.birthDate() != null) {
            patient.setBirthDate(patientCommand.birthDate());
        }
        if (patientCommand.genderCode() != null) {
            patient.setGenderCode(patientCommand.genderCode());
        }
        if (patientCommand.phoneNumber() != null) {
            patient.setPhoneNumber(patientCommand.phoneNumber());
        }
        patientCommand.identifiers().forEach(identifier -> PatientIdentifierSync.upsert(patient, identifier));
        patientCommand.addresses().forEach(address -> upsertAddress(patient, address));
        patientCommand.affiliations().forEach(affiliation -> upsertAffiliation(patient, affiliation));
    }

    /**
     * Matches by {@code id} when the caller supplied one (editing a specific
     * existing address); a missing id falls back to the legacy
     * find-or-create-by-type behavior, which is safe here because
     * {@link uz.uzinfocom.app.modules.patient.domain.enums.AddressType} only
     * has two values, so a patient can only ever have one of each.
     */
    private void upsertAddress(Patient patient, UpdatePatientAddressCommand command) {
        if (command == null || command.type() == null) {
            return;
        }

        PatientAddress address = command.id() != null
                ? findAddressById(patient, command.id())
                : findOrCreateAddressByType(patient, command.type());
        if (address == null) {
            return;
        }

        address.setType(command.type());
        address.setRegionCode(command.regionCode());
        address.setDistrictCode(command.districtCode());
        address.setNeighborhoodCode(command.neighborhoodCode());
        address.setStreetAddress(command.streetAddress());
        address.setHouseNumber(command.houseNumber());
        address.setApartmentNumber(command.apartmentNumber());
    }

    private static PatientAddress findAddressById(Patient patient, Long id) {
        return patient.getAddresses().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private static PatientAddress findOrCreateAddressByType(Patient patient, AddressType type) {
        return patient.getAddresses().stream()
                .filter(item -> type.equals(item.getType()))
                .findFirst()
                .orElseGet(() -> {
                    PatientAddress newAddress = new PatientAddress();
                    newAddress.setType(type);
                    patient.addAddress(newAddress);
                    return newAddress;
                });
    }

    /**
     * Update only ever corrects an existing affiliation, matched by its own id -
     * never creates a new one. A patient's affiliations are set up at
     * registration time; if {@code command.id()} doesn't match one of the
     * patient's current affiliations (including when it's null), the command
     * is silently ignored rather than guessing which affiliation was meant.
     */
    private void upsertAffiliation(Patient patient, UpdatePatientAffiliationCommand command) {
        if (command == null || command.id() == null) {
            return;
        }

        patient.getAffiliations().stream()
                .filter(item -> command.id().equals(item.getId()))
                .findFirst()
                .ifPresent(affiliation -> {
                    affiliation.setType(command.type());
                    affiliation.setLastVisitedDate(command.lastVisitedDate());
                    affiliation.setRegionCode(command.regionCode());
                    affiliation.setDistrictCode(command.districtCode());
                    affiliation.setOrganizationId(command.organizationId());
                    affiliation.setAddress(command.address());
                });
    }
}
