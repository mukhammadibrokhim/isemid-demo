package uz.uzinfocom.app.modules.reference.application.manualreport.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.reference.domain.ManualReport;

@Component
@RequiredArgsConstructor
public class ManualReportQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("manualReportLookupName")
    public String manualReportLookupName(ManualReport manualReport) {
        if (manualReport == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                manualReport.getNameUz(),
                manualReport.getNameUzCyril(),
                manualReport.getNameRu(),
                manualReport.getNameKaa()
        );
    }
}
