package uz.uzinfocom.app.modules.form058.application.query.mapper.helper;

import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableStatus;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058DiagnosisInfoProjection;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.application.query.Form058Direction;
import uz.uzinfocom.app.platform.persistence.mapper.TableStatusMapper;

@Component
public class Form058TableMapperHelper {

    @Named("toTableStatus")
    public Form058TableStatus toTableStatus(
            Form058TableProjection projection,
            @Context Form058Direction direction
    ) {
        if (projection == null) {
            return null;
        }

        FormStatus status = projection.getStatus();

        return TableStatusMapper.deriveTableStatus(
                status,
                direction == Form058Direction.INCOMING && status == FormStatus.SENT,
                Form058TableStatus.NEW,
                Form058TableStatus.class
        );
    }

    // The final_* columns are pre-filled before approval, so they are exposed only for APPROVED forms.
    @Named("toApprovedIcd10Code")
    public String toApprovedIcd10Code(Form058TableProjection projection) {
        Form058DiagnosisInfoProjection diagnosis = approvedDiagnosis(projection);
        return diagnosis != null ? diagnosis.getFinalIcd10Code() : null;
    }

    @Named("toApprovedIcd10Name")
    public String toApprovedIcd10Name(Form058TableProjection projection) {
        Form058DiagnosisInfoProjection diagnosis = approvedDiagnosis(projection);
        return diagnosis != null ? diagnosis.getFinalIcd10Name() : null;
    }

    private Form058DiagnosisInfoProjection approvedDiagnosis(Form058TableProjection projection) {
        if (projection == null || projection.getStatus() != FormStatus.APPROVED) {
            return null;
        }
        return projection.getDiagnosisInfo();
    }
}
