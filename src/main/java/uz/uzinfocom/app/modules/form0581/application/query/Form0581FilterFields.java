package uz.uzinfocom.app.modules.form0581.application.query;

import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.time.LocalDate;

/**
 * Filter fields shared by every Form0581 listing endpoint, regardless of how
 * that listing's access scope is determined - {@link Form0581Filter}
 * (sender/receiver-scoped) and {@link Form0581AffiliatedFilter}
 * (patient-affiliation-scoped) both implement it, so {@code
 * Form0581Specification} can apply this common part once instead of
 * duplicating it per filter type. Deliberately excludes {@code
 * organizationId} and {@code regionCode}/{@code districtCode} - both filter
 * by the sender/receiver institution's identity or location, which has no
 * meaning once access is scoped by patient affiliation instead of
 * direction (the affiliated organization is always the current one). Mirrors
 * {@code Form058FilterFields}.
 */
public interface Form0581FilterFields {

    Form0581Status status();

    LocalDate dateFrom();

    LocalDate dateTo();

    Long id();

    String documentValue();

    String icd10Code();

    String source();
}
