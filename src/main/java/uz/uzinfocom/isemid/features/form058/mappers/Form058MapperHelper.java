package uz.uzinfocom.isemid.features.form058.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class Form058MapperHelper {
    private final OrganizationRepository organizationRepository;
    private final MessageUtil messageUtil;

    @Named("uuidToLongId")
    public Long uuidToLongId(UUID uuid) {
        return organizationRepository.findByUuid(uuid)
                .map(Organization::getId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
    }
}
