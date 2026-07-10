package uz.uzinfocom.app.modules.form0581.application.command.update;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581AnimalInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581AnimalOwnerInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581Address;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581DiagnosisInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581HospitalizationInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581IncidentInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581ReportInfo;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface Form0581UpdateMapper {

    default void update(UpdateForm0581Command command, @MappingTarget Form0581 form0581) {
        if (command == null || form0581 == null) {
            return;
        }

        if (command.receiverOrganizationId() != null) {
            form0581.setReceiverOrganizationId(command.receiverOrganizationId());
        }
        if (command.otherPeopleInjured() != null) {
            form0581.setOtherPeopleInjured(command.otherPeopleInjured());
        }

        updateDiagnosisInfo(command, form0581);
        updateIncidentInfo(command, form0581);
        updateAnimalInfo(command, form0581);
        updateAnimalOwnerInfo(command, form0581);
        updateHospitalizationInfo(command, form0581);
        updateReportInfo(command, form0581);
    }

    default UpdateForm0581Result toResult(Form0581 form0581) {
        if (form0581 == null) {
            return null;
        }

        return new UpdateForm0581Result(
                form0581.getId(),
                form0581.getUuid(),
                form0581.getStatus()
        );
    }

    private void updateDiagnosisInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.mkb10Code() == null && command.mkb10Name() == null && command.injuryLocalization() == null) {
            return;
        }

        if (form0581.getDiagnosisInfo() == null) {
            form0581.setDiagnosisInfo(new Form0581DiagnosisInfo());
        }
        if (command.mkb10Code() != null) {
            form0581.getDiagnosisInfo().setMkb10Code(command.mkb10Code());
        }
        if (command.mkb10Name() != null) {
            form0581.getDiagnosisInfo().setMkb10Name(command.mkb10Name());
        }
        if (command.injuryLocalization() != null) {
            form0581.getDiagnosisInfo().setInjuryLocalization(command.injuryLocalization());
        }
    }

    private void updateIncidentInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.injuryDateTime() == null
                && command.dpuVisitDateTime() == null
                && command.injuryRegionCode() == null
                && command.injuryDistrictCode() == null
                && command.injuryAddress() == null) {
            return;
        }

        if (form0581.getIncidentInfo() == null) {
            form0581.setIncidentInfo(new Form0581IncidentInfo());
        }
        if (command.injuryDateTime() != null) {
            form0581.getIncidentInfo().setInjuryDateTime(command.injuryDateTime());
        }
        if (command.dpuVisitDateTime() != null) {
            form0581.getIncidentInfo().setDpuVisitDateTime(command.dpuVisitDateTime());
        }
        if (command.injuryRegionCode() != null) {
            form0581.getIncidentInfo().setInjuryRegionCode(command.injuryRegionCode());
        }
        if (command.injuryDistrictCode() != null) {
            form0581.getIncidentInfo().setInjuryDistrictCode(command.injuryDistrictCode());
        }
        if (command.injuryAddress() != null) {
            form0581.getIncidentInfo().setInjuryAddress(command.injuryAddress());
        }
    }

    private void updateAnimalInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.animalCategoryCode() == null
                && command.animalColor() == null
                && command.animalType() == null
                && command.animalBreed() == null) {
            return;
        }

        if (form0581.getAnimalInfo() == null) {
            form0581.setAnimalInfo(new Form0581AnimalInfo());
        }
        if (command.animalCategoryCode() != null) {
            form0581.getAnimalInfo().setAnimalCategoryCode(command.animalCategoryCode());
        }
        if (command.animalColor() != null) {
            form0581.getAnimalInfo().setAnimalColor(command.animalColor());
        }
        if (command.animalType() != null) {
            form0581.getAnimalInfo().setAnimalType(command.animalType());
        }
        if (command.animalBreed() != null) {
            form0581.getAnimalInfo().setAnimalBreed(command.animalBreed());
        }
    }

    private void updateAnimalOwnerInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.ownerLastName() == null
                && command.ownerFirstName() == null
                && command.ownerMiddleName() == null
                && command.ownerRegionCode() == null
                && command.ownerDistrictCode() == null
                && command.ownerNeighborhoodCode() == null
                && command.ownerStreet() == null
                && command.ownerHouseNumber() == null
                && command.ownerApartmentNumber() == null) {
            return;
        }

        if (form0581.getAnimalOwnerInfo() == null) {
            form0581.setAnimalOwnerInfo(new Form0581AnimalOwnerInfo());
        }
        if (command.ownerLastName() != null) {
            form0581.getAnimalOwnerInfo().setOwnerLastName(command.ownerLastName());
        }
        if (command.ownerFirstName() != null) {
            form0581.getAnimalOwnerInfo().setOwnerFirstName(command.ownerFirstName());
        }
        if (command.ownerMiddleName() != null) {
            form0581.getAnimalOwnerInfo().setOwnerMiddleName(command.ownerMiddleName());
        }

        if (command.ownerRegionCode() == null
                && command.ownerDistrictCode() == null
                && command.ownerNeighborhoodCode() == null
                && command.ownerStreet() == null
                && command.ownerHouseNumber() == null
                && command.ownerApartmentNumber() == null) {
            return;
        }

        if (form0581.getAnimalOwnerInfo().getOwnerAddress() == null) {
            form0581.getAnimalOwnerInfo().setOwnerAddress(new Form0581Address());
        }
        if (command.ownerRegionCode() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setRegionCode(command.ownerRegionCode());
        }
        if (command.ownerDistrictCode() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setDistrictCode(command.ownerDistrictCode());
        }
        if (command.ownerNeighborhoodCode() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setNeighborhoodCode(command.ownerNeighborhoodCode());
        }
        if (command.ownerStreet() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setStreet(command.ownerStreet());
        }
        if (command.ownerHouseNumber() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setHouseNumber(command.ownerHouseNumber());
        }
        if (command.ownerApartmentNumber() != null) {
            form0581.getAnimalOwnerInfo().getOwnerAddress().setApartmentNumber(command.ownerApartmentNumber());
        }
    }

    private void updateHospitalizationInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.hospitalizedAt() == null && command.hospitalOrganizationId() == null) {
            return;
        }

        if (form0581.getHospitalizationInfo() == null) {
            form0581.setHospitalizationInfo(new Form0581HospitalizationInfo());
        }
        if (command.hospitalizedAt() != null) {
            form0581.getHospitalizationInfo().setHospitalizedAt(command.hospitalizedAt());
        }
        if (command.hospitalOrganizationId() != null) {
            form0581.getHospitalizationInfo().setHospitalOrganizationId(command.hospitalOrganizationId());
        }
    }

    private void updateReportInfo(UpdateForm0581Command command, Form0581 form0581) {
        if (command.antirabicAssistanceInfo() == null
                && command.notifierFullName() == null
                && command.receiverFullName() == null
                && command.messageSentAt() == null) {
            return;
        }

        if (form0581.getReportInfo() == null) {
            form0581.setReportInfo(new Form0581ReportInfo());
        }
        if (command.antirabicAssistanceInfo() != null) {
            form0581.getReportInfo().setAntirabicAssistanceInfo(command.antirabicAssistanceInfo());
        }
        if (command.notifierFullName() != null) {
            form0581.getReportInfo().setNotifierFullName(command.notifierFullName());
        }
        if (command.receiverFullName() != null) {
            form0581.getReportInfo().setReceiverFullName(command.receiverFullName());
        }
        if (command.messageSentAt() != null) {
            form0581.getReportInfo().setMessageSentAt(command.messageSentAt());
        }
    }
}
