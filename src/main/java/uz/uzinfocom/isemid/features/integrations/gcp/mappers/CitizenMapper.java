package uz.uzinfocom.isemid.features.integrations.gcp.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.ChildResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.CitizenResponse;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = ChildMapper.class)
public interface CitizenMapper {

    @Mapping(target = "data", source = "data")
    CitizenResponse mapToResponse(CitizenResponse respCitizen);

    @Mapping(target = "result", constant = "1")
    @Mapping(target = "comments", constant = "")
    @Mapping(target = "data", source = "items")
    CitizenResponse mapToCitizenResponse(ChildResponse response);

    @AfterMapping
    default void transformData(@MappingTarget CitizenResponse.CitizenData data, @Context CatalogService catalogService) {
        if (data.getDocuments() != null && !data.getDocuments().isEmpty()) {
            data.getDocuments().forEach(doc -> {
                switch (doc.getType()) {
                    case "IDMS_RECV_CITIZ_DOCUMENTS" -> doc.setType("PPN");
                    case "IDMS_RECV_IP_DOCUMENTS" -> doc.setType("TPPN");
                    case "IDMS_RECV_LBG_DOCUMENTS" -> doc.setType("FID");
                    case "IDMS_RECV_MVD_IDCARD_CITIZEN", "IDMS_RECV_MVD_IDCARD_NEWBORN" -> doc.setType("CZ");
                    case "IDMS_RECV_MVD_IDCARD_FOREIGN", "IDMS_RECV_MVD_IDCARD_LBG" -> doc.setType("CZF");
                    case "IDMS_RECV_MJ_BIRTH_CERTS" -> doc.setType("BCT");
                }
            });
        }

        if (data.getNationalityid() != 0) {
            Optional<Catalog> nationality = catalogService.findByTypeAndParentCode(CatalogType.nationality, String.valueOf(data.getNationalityid()));
            nationality.ifPresent(value -> data.setNationalityid(Integer.parseInt(value.getCode())));
        }

        if (data.getCitizenshipid() != null && !data.getCitizenshipid().isEmpty()) {
            Optional<Catalog> country = catalogService.findByTypeAndParentCode(CatalogType.country, data.getCitizenshipid());
            country.ifPresent(value -> data.setCitizenshipid(value.getCode()));
        }

        if (data.getBirthcountryid() != null && !data.getBirthcountryid().isEmpty()) {
            Optional<Catalog> country = catalogService.findByTypeAndParentCode(CatalogType.country, data.getBirthcountryid());
            country.ifPresent(value -> data.setBirthcountryid(value.getCode()));
        }

        if (data.getLivestatus() != null && !data.getLivestatus().isEmpty()) {
            data.setLivestatus(data.getLivestatus().equals("1") ? "false" : "true");
        }

        if (data.getSex() != null && !data.getSex().isEmpty()) {
            data.setSex(data.getSex().equals("1") ? "male" : "female");
        }
    }
}
