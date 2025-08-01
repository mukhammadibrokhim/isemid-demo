package uz.uzinfocom.isemid.features.form058.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Form058MapperHelper {
    private final OrganizationRepository organizationRepository;

    @Named("uuidToLongId")
    public Long uuidToLongId(UUID uuid) {
        return organizationRepository.findByUuid(uuid)
                .map(Organization::getId)
                .orElseThrow(() -> new RuntimeException("HospitalPlace not found by UUID: " + uuid));
    }
}
