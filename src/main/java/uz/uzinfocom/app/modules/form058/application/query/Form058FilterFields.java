package uz.uzinfocom.app.modules.form058.application.query;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.time.LocalDate;

/**
 * Filter fields shared by every Form058 listing endpoint, regardless of how
 * that listing's access scope is determined - {@link Form058Filter}
 * (sender/receiver-scoped) and {@link Form058AffiliatedFilter}
 * (patient-affiliation-scoped) both implement it, so {@code
 * Form058Specification} can apply this common part once instead of
 * duplicating it per filter type. Deliberately excludes {@code
 * organizationId} and {@code regionCode}/{@code districtCode} - both filter
 * by the sender/receiver institution's identity or location, which has no
 * meaning once access is scoped by patient affiliation instead of
 * direction (the affiliated organization is always the current one).
 */
public interface Form058FilterFields {

    FormStatus status();

    LocalDate dateFrom();

    LocalDate dateTo();

    Long id();

    String documentValue();

    String icd10Code();

    String source();

    Boolean hasLinkedCards();
}
