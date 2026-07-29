package uz.uzinfocom.app.modules.card.domain.enums;

/**
 * Which owning case a {@code Card} (or, one hop further, an {@code Act})
 * belongs to — a card belongs to exactly one of {@code Form058}/{@code
 * Form0581}, never both. Used to restrict stats/scope queries to one case
 * type (e.g. the form058 dashboard's own card/act breakdown) or to include
 * both ({@link #ANY}, e.g. the standalone card/act dashboards).
 */
public enum CaseFormType {
    ANY,
    FORM058,
    FORM0581
}
