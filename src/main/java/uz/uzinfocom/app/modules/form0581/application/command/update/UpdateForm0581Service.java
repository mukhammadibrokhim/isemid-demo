package uz.uzinfocom.app.modules.form0581.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.command.Form0581OtherInjuredPersonMapper;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.service.PatientIdentifierSync;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditFieldDiff;
import uz.uzinfocom.app.platform.audit.event.AffiliatedOrganizationsAddedEvent;
import uz.uzinfocom.app.platform.audit.event.FieldsChangedEvent;
import uz.uzinfocom.app.platform.audit.event.OrganizationReassignedEvent;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;
import uz.uzinfocom.app.platform.scope.FormAccessScopeResolver;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UpdateForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final Form0581UpdateValidator form0581UpdateValidator;
    private final Form0581OtherInjuredPersonMapper otherInjuredPersonMapper;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm0581Result update(UpdateForm0581Command command) {
        Form0581 form0581 = findRequired(command.id());
        form0581UpdateValidator.validate(form0581, command);
        Long oldReceiverOrganizationId = form0581.getReceiverOrganizationId();
        Map<String, Object> before = form0581.auditFields();
        Set<Long> affiliationsBefore = affiliatedOrganizationIds(form0581.getPatient());
        form0581UpdateMapper.update(command, form0581);

        if (command.otherInjuredPeople() != null) {
            ChildCollectionSync.sync(
                    form0581,
                    form0581.getOtherInjuredPeople(),
                    command.otherInjuredPeople(),
                    otherInjuredPersonMapper::toEntity,
                    otherInjuredPersonMapper::update,
                    Form0581OtherInjuredPerson::setForm0581
            );
        }

        updatePatient(command.patient(), form0581.getPatient());
        UpdateForm0581Result result = form0581UpdateMapper.toResult(form0581Repository.save(form0581));

        Long actorUserId = currentUserProvider.userIdOrNull();
        Long newReceiverOrganizationId = form0581.getReceiverOrganizationId();
        if (!Objects.equals(oldReceiverOrganizationId, newReceiverOrganizationId)) {
            eventPublisher.publishEvent(new OrganizationReassignedEvent(
                    AuditEntityType.FORM0581, form0581.getId(),
                    oldReceiverOrganizationId, newReceiverOrganizationId,
                    actorUserId
            ));
        }

        Map<String, Object> changes = AuditFieldDiff.compute(before, form0581.auditFields());
        if (!changes.isEmpty()) {
            eventPublisher.publishEvent(new FieldsChangedEvent(
                    AuditEntityType.FORM0581, form0581.getId(), changes, actorUserId
            ));
        }

        Set<Long> newlyAffiliatedOrganizationIds = affiliatedOrganizationIds(form0581.getPatient());
        newlyAffiliatedOrganizationIds.removeAll(affiliationsBefore);
        newlyAffiliatedOrganizationIds.remove(form0581.getSenderOrganizationId());
        newlyAffiliatedOrganizationIds.remove(newReceiverOrganizationId);
        if (!newlyAffiliatedOrganizationIds.isEmpty()) {
            eventPublisher.publishEvent(new AffiliatedOrganizationsAddedEvent(
                    AuditEntityType.FORM0581, form0581.getId(), List.copyOf(newlyAffiliatedOrganizationIds), actorUserId
            ));
        }

        return result;
    }

    private Form0581 findRequired(Long id) {
        return form0581Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form0581NotFoundException(id));
    }

    /**
     * Same before/after affiliation snapshot as {@code
     * UpdateForm058Service#affiliatedOrganizationIds} - see its javadoc.
     */
    private Set<Long> affiliatedOrganizationIds(Patient patient) {
        if (patient == null) {
            return new HashSet<>();
        }
        Set<Long> organizationIds = new HashSet<>();
        for (PatientAffiliation affiliation : patient.getAffiliations()) {
            if (FormAccessScopeResolver.AFFILIATION_TYPES.contains(affiliation.getType())
                    && affiliation.getOrganizationId() != null) {
                organizationIds.add(affiliation.getOrganizationId());
            }
        }
        return organizationIds;
    }

    /**
     * Same patient sync as {@code UpdateForm058Service.updatePatient} —
     * demographic fields, identifiers, addresses, and affiliations are all
     * upserted here too.
     */
    private void updatePatient(UpdatePatientCommand patientCommand, Patient patient) {
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
        if (patientCommand.kinshipDegree() != null) {
            patient.setKinshipDegree(patientCommand.kinshipDegree());
        }
        if (patientCommand.kinshipFullName() != null) {
            patient.setKinshipFullName(patientCommand.kinshipFullName());
        }
        if (patientCommand.residentialStatusCode() != null) {
            patient.setResidentialStatusCode(patientCommand.residentialStatusCode());
        }
        if (patientCommand.maritalStatusCode() != null) {
            patient.setMaritalStatusCode(patientCommand.maritalStatusCode());
        }
        if (patientCommand.populationTypeCode() != null) {
            patient.setPopulationTypeCode(patientCommand.populationTypeCode());
        }
        if (patientCommand.categoryCode() != null) {
            patient.setCategoryCode(patientCommand.categoryCode());
        }
        if (patientCommand.professionCode() != null) {
            patient.setProfessionCode(patientCommand.professionCode());
        }

        patientCommand.identifiers().forEach(identifier -> PatientIdentifierSync.upsert(patient, identifier));
        patientCommand.addresses().forEach(address -> upsertAddress(patient, address));
        patientCommand.affiliations().forEach(affiliation -> upsertAffiliation(patient, affiliation));
    }

    /**
     * Same "id optional, update-in-place when it matches, otherwise add new"
     * rule as {@code UpdateForm058Service#upsertAddress} - see its javadoc.
     */
    private void upsertAddress(Patient patient, UpdatePatientAddressCommand command) {
        if (command == null || command.type() == null) {
            return;
        }

        PatientAddress address;
        if (command.id() != null) {
            address = findAddressById(patient, command.id());
            if (address == null) {
                return;
            }
        } else {
            address = new PatientAddress();
            patient.addAddress(address);
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

    /**
     * Same "id optional, update-in-place when it matches, otherwise add new"
     * rule as {@code UpdateForm058Service#upsertAffiliation} - see its
     * javadoc.
     */
    private void upsertAffiliation(Patient patient, UpdatePatientAffiliationCommand command) {
        if (command == null || command.type() == null) {
            return;
        }

        PatientAffiliation affiliation;
        if (command.id() != null) {
            affiliation = patient.getAffiliations().stream()
                    .filter(item -> command.id().equals(item.getId()))
                    .findFirst()
                    .orElse(null);
            if (affiliation == null) {
                return;
            }
        } else {
            affiliation = new PatientAffiliation();
            patient.addAffiliation(affiliation);
        }

        affiliation.setType(command.type());
        affiliation.setLastVisitedDate(command.lastVisitedDate());
        affiliation.setRegionCode(command.regionCode());
        affiliation.setDistrictCode(command.districtCode());
        affiliation.setOrganizationId(command.organizationId());
        affiliation.setAddress(command.address());
    }
}
