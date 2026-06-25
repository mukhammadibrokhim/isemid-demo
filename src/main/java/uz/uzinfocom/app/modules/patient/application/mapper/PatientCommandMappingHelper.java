package uz.uzinfocom.app.modules.patient.application.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PatientCommandMappingHelper {

    @Named("toPatientIdentifiers")
    public List<PatientIdentifier> toPatientIdentifiers(List<CreatePatientIdentifierCommand> commands) {
        List<PatientIdentifier> identifiers = new ArrayList<>();
        if (commands == null) {
            return identifiers;
        }

        commands.stream()
                .map(this::toIdentifier)
                .filter(Objects::nonNull)
                .forEach(identifiers::add);
        return identifiers;
    }

    @Named("toPatientAddresses")
    public List<PatientAddress> toPatientAddresses(List<CreatePatientAddressCommand> commands) {
        List<PatientAddress> addresses = new ArrayList<>();
        if (commands == null) {
            return addresses;
        }

        commands.stream()
                .map(this::toAddress)
                .filter(Objects::nonNull)
                .forEach(addresses::add);
        return addresses;
    }

    @Named("toPatientAffiliations")
    public List<PatientAffiliation> toPatientAffiliations(List<CreatePatientAffiliationCommand> commands) {
        List<PatientAffiliation> affiliations = new ArrayList<>();
        if (commands == null) {
            return affiliations;
        }

        commands.stream()
                .map(this::toAffiliation)
                .filter(Objects::nonNull)
                .forEach(affiliations::add);
        return affiliations;
    }

    @AfterMapping
    @Named("linkPatientChildren")
    public Patient linkPatientChildren(@MappingTarget Patient patient) {
        if (patient == null) {
            return null;
        }

        if (patient.getIdentifiers() != null) {
            patient.getIdentifiers().forEach(item -> item.setPatient(patient));
        }
        if (patient.getAddresses() != null) {
            patient.getAddresses().forEach(item -> item.setPatient(patient));
        }
        if (patient.getAffiliations() != null) {
            patient.getAffiliations().forEach(item -> item.setPatient(patient));
        }

        return patient;
    }

    private PatientIdentifier toIdentifier(CreatePatientIdentifierCommand command) {
        if (command == null) {
            return null;
        }

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setTypeCode(command.type());
        identifier.setValue(command.value());
        identifier.setPeriodStart(command.periodStart());
        identifier.setPeriodEnd(command.periodEnd());
        return identifier;
    }

    private PatientAddress toAddress(CreatePatientAddressCommand command) {
        if (command == null) {
            return null;
        }

        PatientAddress address = new PatientAddress();
        address.setType(command.type());
        address.setRegionCode(command.regionCode());
        address.setDistrictCode(command.districtCode());
        address.setNeighborhoodCode(command.neighborhoodCode());
        address.setStreetAddress(command.streetAddress());
        address.setHouseNumber(command.houseNumber());
        address.setApartmentNumber(command.apartmentNumber());
        return address;
    }

    private PatientAffiliation toAffiliation(CreatePatientAffiliationCommand command) {
        if (command == null) {
            return null;
        }

        PatientAffiliation affiliation = new PatientAffiliation();
        affiliation.setType(command.type());
        affiliation.setLastVisitedDate(command.lastVisitedDate());
        affiliation.setOrganizationName(command.organizationName());
        affiliation.setRegionCode(command.regionCode());
        affiliation.setDistrictCode(command.cityCode());
        affiliation.setOrganizationId(command.organizationId());
        affiliation.setOrganizationUuid(command.organizationUuid());
        affiliation.setAddress(command.address());
        return affiliation;
    }
}
