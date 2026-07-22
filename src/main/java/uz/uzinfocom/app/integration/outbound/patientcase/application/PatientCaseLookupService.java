package uz.uzinfocom.app.integration.outbound.patientcase.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.integration.outbound.patientcase.web.dto.PatientCaseResponse;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification.Form058Specification;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification.Form0581Specification;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

/**
 * Outbound counterpart to the inbound form058/form058-1 submission
 * endpoints: instead of an external system pushing case data to us, a
 * registered integration client pulls a patient's basic info plus their most
 * recently submitted form058/form058-1 back out.
 * <p>
 * Patient records are not deduplicated across separate submissions in this
 * system - the same real person's PINFL/NNUZB can end up attached to
 * several distinct {@code Patient} rows, one per submission. Resolving a
 * single "the patient" row first and then looking up their forms would
 * silently miss forms attached to any other duplicate, so the form lookups
 * below match directly on identifier value (see
 * {@code Form058Specification#visibleByDocumentValueAndOrganization}) and
 * the patient summary returned is whichever of the two matched forms is
 * more recent - not an independently resolved "canonical" patient.
 * <p>
 * Visibility of each form is scoped to the caller's own organization
 * (sender or receiver), exactly like the frontend's own scoped queries.
 */
@Service
@RequiredArgsConstructor
public class PatientCaseLookupService {

    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final Form058JpaRepository form058JpaRepository;
    private final Form0581JpaRepository form0581JpaRepository;
    private final Form058Specification form058Specification;
    private final Form0581Specification form0581Specification;
    private final PatientCaseMapper patientCaseMapper;

    @Transactional(readOnly = true)
    public PatientCaseResponse lookup(String identifierValue, Long organizationId) {
        Form058 latestForm058 = Objects.requireNonNull(form058JpaRepository.findBy(
                form058Specification.visibleByDocumentValueAndOrganization(identifierValue, organizationId),
                query -> query.sortBy(MOST_RECENT_FIRST).first()
        )).orElse(null);

        Form0581 latestForm0581 = Objects.requireNonNull(form0581JpaRepository.findBy(
                form0581Specification.visibleByDocumentValueAndOrganization(identifierValue, organizationId),
                query -> query.sortBy(MOST_RECENT_FIRST).first()
        )).orElse(null);

        if (latestForm058 == null && latestForm0581 == null) {
            throw new NotFoundException("integration.patient-case.not-found");
        }

        Patient patient = resolveMostRecentPatient(latestForm058, latestForm0581);

        return patientCaseMapper.toResponse(patient, latestForm058, latestForm0581);
    }

    private Patient resolveMostRecentPatient(Form058 latestForm058, Form0581 latestForm0581) {
        if (latestForm058 == null) {
            return latestForm0581.getPatient();
        }

        if (latestForm0581 == null) {
            return latestForm058.getPatient();
        }

        return latestForm058.getCreatedAt().isAfter(latestForm0581.getCreatedAt())
                ? latestForm058.getPatient()
                : latestForm0581.getPatient();
    }
}
