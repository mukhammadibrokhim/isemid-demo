package uz.uzinfocom.app.integration.lis.client.dto;

/**
 * How urgently LIS should process the act. Chosen by the employee in the
 * send dialog, not derived from the act's own data.
 */
public enum LisPriority {
    IMMEDIATE,
    URGENT,
    SCHEDULED
}
