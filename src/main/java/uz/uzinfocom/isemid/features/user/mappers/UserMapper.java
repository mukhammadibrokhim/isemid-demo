package uz.uzinfocom.isemid.features.user.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.MisUserResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMeResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserResponse;
import uz.uzinfocom.isemid.features.user.models.User;

@Mapper(
        componentModel = "spring",
        uses = {CatalogMapperHelper.class, RoleMapper.class, OrganizationMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "gender", source = "genderCode", qualifiedByName = "toCatalog")
    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "organizations", source = "organizations")
    UserResponse toResponse(User user);

    @Mapping(target = "gender", source = "genderCode", qualifiedByName = "toCatalog")
    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "organizations", source = "organizations")
    @Mapping(target = "currentOrganization", source = "currentOrganization")
    UserMeResponse toUserMeResponse(User user);

    UserMiniResponse toMiniResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "name.firstName", target = "firstName")
    @Mapping(source = "name.lastName", target = "lastName")
    @Mapping(source = "name.patronymic", target = "middleName")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "active", target = "active", defaultValue = "false")
    @Mapping(source = "gender", target = "genderCode")
    @Mapping(target = "phoneNumber", expression = "java(response.getTelecom() != null && !response.getTelecom().isEmpty() ? response.getTelecom().get(0).getValue() : \"\")")
    @Mapping(target = "countryCode", expression = "java(response.getAddress() != null && !response.getAddress().isEmpty() ? response.getAddress().get(0).getCountry() : \"\")")
    @Mapping(target = "stateCode", expression = "java(response.getAddress() != null && !response.getAddress().isEmpty() ? response.getAddress().get(0).getState() : \"\")")
    @Mapping(target = "cityCode", expression = "java(response.getAddress() != null && !response.getAddress().isEmpty() ? response.getAddress().get(0).getCity() : \"\")")
    @Mapping(target = "district", expression = "java(response.getAddress() != null && !response.getAddress().isEmpty() ? response.getAddress().get(0).getDistrict() : \"\")")
    @Mapping(target = "line", expression = "java(response.getAddress() != null && !response.getAddress().isEmpty() ? response.getAddress().get(0).getLine() : \"\")")
    @Mapping(target = "nnuzb", expression = "java(extractIdentifierByType(response, \"NNUZB\"))")
    @Mapping(target = "ppn", expression = "java(extractIdentifierByType(response, \"PPN\"))")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "currentOrganization", ignore = true)
    @Mapping(target = "positionCode", ignore = true)
    User toUser(MisUserResponse response);

    @Mapping(target = "genderCode", source = "gender.value", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserRequest dto, @MappingTarget User entity);



    default String extractIdentifierByType(MisUserResponse response, String typeCode) {
        if (response == null || response.getIdentifier() == null || response.getIdentifier().isEmpty()) {
            System.out.println("response.getIdentifier() is null or empty");
            return null;
        }

        return response.getIdentifier().stream()
                .filter(identifier ->
                        identifier.getType() != null &&
                                identifier.getType().getCoding() != null &&
                                identifier.getType().getCoding().stream()
                                        .anyMatch(coding -> typeCode.equals(coding.getCode()))
                )
                .map(MisUserResponse.Identifier::getValue)
                .findFirst()
                .orElse(null);
    }
}
