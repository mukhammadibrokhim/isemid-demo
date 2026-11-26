package uz.uzinfocom.app.modules.form0581.application.query.mapper.helper;

import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableStatus;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581DiagnosisInfoProjection;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Direction;
import uz.uzinfocom.app.platform.persistence.mapper.TableStatusMapper;

@Component
public class Form0581TableMapperHelper {

    @Named("toTableStatus")
    public Form0581TableStatus toTableStatus(
            Form0581TableProjection projection,
            @Context Form0581Direction direction
    ) {
        if (projection == null) {
            return null;
        }

        Form0581Status status = projection.getStatus();

        return TableStatusMapper.deriveTableStatus(
                status,
                direction == Form0581Direction.INCOMING && status == Form0581Status.SENT,
                Form0581TableStatus.NEW,
                Form0581TableStatus.class
        );
    }

    // The final_* columns are pre-filled before approval, so they are exposed only for APPROVED forms.
    @Named("toApprovedIcd10Code")
    public String toApprovedIcd10Code(Form0581TableProjection projection) {
        Form0581DiagnosisInfoProjection diagnosis = approvedDiagnosis(projection);
        return diagnosis != null ? diagnosis.getFinalIcd10Code() : null;
    }

    @Named("toApprovedIcd10Name")
    public String toApprovedIcd10Name(Form0581TableProjection projection) {
        Form0581DiagnosisInfoProjection diagnosis = approvedDiagnosis(projection);
        return diagnosis != null ? diagnosis.getFinalIcd10Name() : null;
    }

    private Form0581DiagnosisInfoProjection approvedDiagnosis(Form0581TableProjection projection) {
        if (projection == null || projection.getStatus() != Form0581Status.APPROVED) {
            return null;
        }
        return projection.getDiagnosisInfo();
    }
}
