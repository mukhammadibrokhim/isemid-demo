package uz.uzinfocom.app.modules.form0581.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.command.Form0581OtherInjuredPersonMapper;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.service.PatientIdentifierSync;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Service
@RequiredArgsConstructor
public class UpdateForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final Form0581UpdateValidator form0581UpdateValidator;
    private final Form0581OtherInjuredPersonMapper otherInjuredPersonMapper;

    @Transactional
    public UpdateForm0581Result update(UpdateForm0581Command command) {
        Form0581 form0581 = findRequired(command.id());
        form0581UpdateValidator.validate(form0581, command);
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
        return form0581UpdateMapper.toResult(form0581Repository.save(form0581));
    }

    private Form0581 findRequired(Long id) {
        return form0581Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form0581NotFoundException(id));
    }

    /**
     * Unlike Form058's patient sync (name/birthDate/gender/phone/identifiers
     * only), this also upserts addresses and affiliations — address and
     * employment/education are first-class fields on this form, not
     * incidental ones.
     */
    private void updatePatient(CreatePatientCommand patientCommand, Patient patient) {
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

    private void upsertAddress(Patient patient, CreatePatientAddressCommand command) {
        if (command == null || command.type() == null) {
            return;
        }

        PatientAddress address = patient.getAddresses().stream()
                .filter(item -> command.type().equals(item.getType()))
                .findFirst()
                .orElseGet(() -> {
                    PatientAddress newAddress = new PatientAddress();
                    newAddress.setType(command.type());
                    patient.addAddress(newAddress);
                    return newAddress;
                });
        address.setRegionCode(command.regionCode());
        address.setDistrictCode(command.districtCode());
        address.setNeighborhoodCode(command.neighborhoodCode());
        address.setStreetAddress(command.streetAddress());
        address.setHouseNumber(command.houseNumber());
        address.setApartmentNumber(command.apartmentNumber());
    }

    private void upsertAffiliation(Patient patient, CreatePatientAffiliationCommand command) {
        if (command == null || command.type() == null) {
            return;
        }

        PatientAffiliation affiliation = patient.getAffiliations().stream()
                .filter(item -> command.type().equals(item.getType()))
                .findFirst()
                .orElseGet(() -> {
                    PatientAffiliation newAffiliation = new PatientAffiliation();
                    newAffiliation.setType(command.type());
                    patient.addAffiliation(newAffiliation);
                    return newAffiliation;
                });
        affiliation.setLastVisitedDate(command.lastVisitedDate());
        affiliation.setOrganizationName(command.organizationName());
        affiliation.setRegionCode(command.regionCode());
        affiliation.setDistrictCode(command.cityCode());
        affiliation.setOrganizationId(command.organizationId());
        affiliation.setOrganizationUuid(command.organizationUuid());
        affiliation.setAddress(command.address());
    }
}
