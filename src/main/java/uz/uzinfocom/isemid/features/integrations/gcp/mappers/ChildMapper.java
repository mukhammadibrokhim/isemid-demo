package uz.uzinfocom.isemid.features.integrations.gcp.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.ChildResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.CitizenResponse;
import uz.uzinfocom.isemid.features.patient.enums.IdentifierType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChildMapper {

    @Mapping(target = "transaction_id", constant = "1")
    @Mapping(target = "current_pinpp", source = "pnfl")
    @Mapping(target = "pinpps", expression = "java(getDefaultPinpps(child.getPnfl()))")
    @Mapping(target = "current_document", expression = "java(child.getCert_series() + child.getCert_number())")
    @Mapping(target = "documents", expression = "java(getDocuments(child))")
    @Mapping(target = "surnamelat", source = "surname")
    @Mapping(target = "namelat", source = "name")
    @Mapping(target = "patronymlat", source = "patronym")
    @Mapping(target = "surnamecyr", source = "surname")
    @Mapping(target = "namecyr", source = "name")
    @Mapping(target = "patronymcyr", source = "patronym")
    @Mapping(target = "birth_date", source = "birth_date")
    @Mapping(target = "sex", expression = "java(child.getGender_code() == 1 ? \"male\" : \"female\")")
    CitizenResponse.CitizenData mapToCitizenData(ChildResponse.Items child);

    @AfterMapping
    default void setDocuments(@MappingTarget CitizenResponse.CitizenData citizenData, ChildResponse.Items child) {
        CitizenResponse.Document document = CitizenResponse.Document.builder()
                .document(child.getCert_series() + child.getCert_number())
                .type(IdentifierType.BCT.name())
                .docgiveplaceid(child.getBranch())
                .datebegin(child.getDoc_date())
                .build();

        citizenData.setDocuments(List.of(document));
    }
    default List<String> getDefaultPinpps(String pnfl) {
        return pnfl != null ? Collections.singletonList(pnfl) : Collections.emptyList();
    }

    default List<CitizenResponse.Document> getDocuments(ChildResponse.Items child) {
        List<CitizenResponse.Document> documents = new ArrayList<>();

        if (child.getCert_series() != null && child.getCert_number() != null) {
            documents.add(CitizenResponse.Document.builder()
                    .type(IdentifierType.BCT.name())
                    .document(child.getCert_series() + child.getCert_number())
                    .docgiveplaceid(child.getBranch())
                    .datebegin(child.getDoc_date())
                    .build());
        }

        return documents;
    }


}
