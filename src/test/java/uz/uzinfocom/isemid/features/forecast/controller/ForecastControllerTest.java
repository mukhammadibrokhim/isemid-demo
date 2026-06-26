package uz.uzinfocom.isemid.features.forecast.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWeeklyResponse;
import uz.uzinfocom.isemid.features.forecast.models.dto.HoltWintersResponse;
import uz.uzinfocom.isemid.features.forecast.service.Form058HoltWeeklyForecastFacade;
import uz.uzinfocom.isemid.features.forecast.service.Form058HoltWintersFacade;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ForecastControllerTest {

    private MockMvc mockMvc;

    private final Form058HoltWeeklyForecastFacade facade   = mock(Form058HoltWeeklyForecastFacade.class);
    private final Form058HoltWintersFacade         facadeV2 = mock(Form058HoltWintersFacade.class);
    private final MessageUtil messageUtil = mock(MessageUtil.class);

    private static final HoltWeeklyResponse  STUB_V1 = new HoltWeeklyResponse(0.3, 0.1, List.of());
    private static final HoltWintersResponse STUB_V2 = new HoltWintersResponse(
            0.3, 0.1, 0.2, 12, List.of(), List.of(),
            new HoltWintersResponse.Metrics(2.5, "Аъло"));

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ForecastController(facade, facadeV2, messageUtil))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        when(messageUtil.get("message.success")).thenReturn("OK");
        when(facade.holt(any(), any(), any(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(STUB_V1);
        when(facadeV2.holt(any(), any(), any(), anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyInt(), any(), any()))
                .thenReturn(STUB_V2);
    }

    // ── holtWeekly (v1) ───────────────────────────────────────────────────────

    @Test
    void holtWeekly_requiredParams_returns200() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alpha").value(0.3))
                .andExpect(jsonPath("$.data.beta").value(0.1));
    }

    @Test
    void holtWeekly_missingAlpha_returns400() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("beta",      "0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void holtWeekly_missingBeta_returns400() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void holtWeekly_missingStartDate_returns400() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("alpha", "0.3")
                        .param("beta",  "0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void holtWeekly_optionalEndDateOmitted_passesNullToFacade() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1"))
                .andExpect(status().isOk());

        verify(facade).holt(any(),
                eq(LocalDate.of(2024, 1, 1)), isNull(),
                eq(0.3), eq(0.1), isNull(), isNull());
    }

    @Test
    void holtWeekly_optionalFilters_passedToFacade() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1")
                        .param("stateCode", "UZB")
                        .param("mkb10C0de", "A01"))
                .andExpect(status().isOk());

        verify(facade).holt(any(), any(), any(), anyDouble(), anyDouble(), eq("UZB"), eq("A01"));
    }

    @Test
    void holtWeekly_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(get("/v1/forecast/holt-weekly")
                        .param("startDate", "01-01-2024")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1"))
                .andExpect(status().isBadRequest());
    }

    // ── holtWeeklyV2 ─────────────────────────────────────────────────────────

    @Test
    void holtWeeklyV2_requiredParams_returns200() throws Exception {
        mockMvc.perform(get("/v2/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1")
                        .param("gamma",     "0.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gamma").value(0.2))
                .andExpect(jsonPath("$.data.season").value(12))
                .andExpect(jsonPath("$.data.metrics.quality").value("Аъло"));
    }

    @Test
    void holtWeeklyV2_missingGamma_returns400() throws Exception {
        mockMvc.perform(get("/v2/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void holtWeeklyV2_defaultSeasonAndHorizon_passesTwelveToFacade() throws Exception {
        mockMvc.perform(get("/v2/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1")
                        .param("gamma",     "0.2"))
                .andExpect(status().isOk());

        ArgumentCaptor<Integer> seasonCaptor  = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> horizonCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(facadeV2).holt(any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(),
                seasonCaptor.capture(), horizonCaptor.capture(), any(), any());

        assertThat(seasonCaptor.getValue()).isEqualTo(12);
        assertThat(horizonCaptor.getValue()).isEqualTo(12);
    }

    @Test
    void holtWeeklyV2_explicitSeasonAndHorizon_passedToFacade() throws Exception {
        mockMvc.perform(get("/v2/forecast/holt-weekly")
                        .param("startDate", "2024-01-01")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1")
                        .param("gamma",     "0.2")
                        .param("season",    "4")
                        .param("horizon",   "8"))
                .andExpect(status().isOk());

        verify(facadeV2).holt(any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(), eq(4), eq(8), any(), any());
    }

    @Test
    void holtWeeklyV2_datesAreParsedCorrectly() throws Exception {
        mockMvc.perform(get("/v2/forecast/holt-weekly")
                        .param("startDate", "2023-06-15")
                        .param("endDate",   "2025-11-30")
                        .param("alpha",     "0.3")
                        .param("beta",      "0.1")
                        .param("gamma",     "0.2"))
                .andExpect(status().isOk());

        verify(facadeV2).holt(any(),
                eq(LocalDate.of(2023, 6, 15)),
                eq(LocalDate.of(2025, 11, 30)),
                anyDouble(), anyDouble(), anyDouble(),
                anyInt(), anyInt(), any(), any());
    }
}
