package uz.uzinfocom.app.modules.reference.application.manualreport.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.modules.reference.application.common.event.ManualReportChangedEvent;
import uz.uzinfocom.app.modules.reference.application.manualreport.dto.ManualReportCreateRequest;
import uz.uzinfocom.app.modules.reference.application.manualreport.dto.ManualReportUpdateRequest;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.mapper.ManualReportMapper;
import uz.uzinfocom.app.modules.reference.domain.ManualReport;
import uz.uzinfocom.app.modules.reference.repository.ManualReportRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualReportCommandService {

    private final ManualReportRepository manualReportRepository;
    private final ManualReportMapper manualReportMapper;
    private final ManualReportCommandMapper manualReportCommandMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ManualReportValidator manualReportValidator;

    @Transactional
    public ManualReportResponse create(ManualReportCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());

        if (manualReportRepository.existsByCode(code)) {
            throw new ConflictException("reference.manual_report.code.already_exists", code);
        }

        manualReportValidator.validate(
                manualReportCommandMapper.normalizeTags(request.reportTypes()),
                manualReportCommandMapper.normalizeIcd10Codes(request.icd10Codes()),
                Set.of(),
                Set.of()
        );

        manualReportValidator.validateAgeBounds(
                request.ageFrom(), request.ageFromUnit(), request.ageTo(), request.ageToUnit()
        );
        manualReportValidator.validateCaseFilters(request.caseSource(), request.labConfirmation());
        manualReportValidator.validateDiseasePlaces(
                manualReportCommandMapper.normalizeTags(request.diseasePlaceCodes()),
                Set.of(),
                request.caseSource()
        );

        ManualReport manualReport = manualReportCommandMapper.toEntity(request);

        ManualReport saved = manualReportRepository.save(manualReport);
        eventPublisher.publishEvent(new ManualReportChangedEvent());
        log.debug("Reference manual report created. id={}, code={}", saved.getId(), saved.getCode());

        return manualReportMapper.toResponse(saved);
    }

    @Transactional
    public ManualReportResponse update(Long id, ManualReportUpdateRequest request) {
        ManualReport manualReport = manualReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.manual_report.not_found_by_id", id));

        if (manualReport.isDeleted()) {
            throw new ConflictException("reference.manual_report.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());

        if (!Objects.equals(manualReport.getCode(), code) && manualReportRepository.existsByCode(code)) {
            throw new ConflictException("reference.manual_report.code.already_exists", code);
        }

        // Must run before updateEntity: it compares against what is stored now.
        manualReportValidator.validate(
                manualReportCommandMapper.normalizeTags(request.reportTypes()),
                manualReportCommandMapper.normalizeIcd10Codes(request.icd10Codes()),
                manualReport.getReportTypes(),
                manualReport.getIcd10Codes()
        );

        manualReportValidator.validateAgeBounds(
                request.ageFrom(), request.ageFromUnit(), request.ageTo(), request.ageToUnit()
        );
        manualReportValidator.validateCaseFilters(request.caseSource(), request.labConfirmation());
        manualReportValidator.validateDiseasePlaces(
                manualReportCommandMapper.normalizeTags(request.diseasePlaceCodes()),
                manualReport.getDiseasePlaceCodes(),
                request.caseSource()
        );

        manualReportCommandMapper.updateEntity(manualReport, request);

        ManualReport saved = manualReportRepository.save(manualReport);
        eventPublisher.publishEvent(new ManualReportChangedEvent());
        log.debug("Reference manual report updated. id={}, code={}", saved.getId(), saved.getCode());

        return manualReportMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        ManualReport manualReport = manualReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.manual_report.not_found_by_id", id));

        if (manualReport.isDeleted()) {
            return;
        }

        manualReport.setDeleted(true);
        manualReportRepository.save(manualReport);
        eventPublisher.publishEvent(new ManualReportChangedEvent());
        log.debug("Reference manual report soft-deleted. id={}, code={}", manualReport.getId(), manualReport.getCode());
    }
}
