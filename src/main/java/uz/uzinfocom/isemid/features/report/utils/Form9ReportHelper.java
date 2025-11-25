package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Form9ReportHelper {
    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public Object buildByParentCode(String trim, List<Form058> forms) {
        return null;
    }

    public Object buildRegionReports(List<Form058> forms) {
        return null;
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {
        return null;
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {
        return null;
    }
}
