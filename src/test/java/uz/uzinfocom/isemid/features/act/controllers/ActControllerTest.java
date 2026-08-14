package uz.uzinfocom.isemid.features.act.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ProtocolConclusionRequest;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.services.ActService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActControllerTest {

    private MockMvc mockMvc;

    private final ActService actService = mock(ActService.class);
    private final MessageUtil messageUtil = mock(MessageUtil.class);
    private final ActMapperFactory factory = mock(ActMapperFactory.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ActController(actService, messageUtil, factory))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        when(messageUtil.get("message.success")).thenReturn("OK");
    }

    // ── GET /{id}/lis-confirmation ────────────────────────────────

    @Test
    void getReviewedAct_returnsDocumentFromService() throws Exception {
        Map<String, Object> reviewedDocument = Map.of("status", "ACCEPTED");
        when(actService.getReviewedDocument(42L)).thenReturn(reviewedDocument);

        mockMvc.perform(get("/v1/act/42/lis-confirmation"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        verify(actService).getReviewedDocument(42L);
    }

    @Test
    void getReviewedAct_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/v1/act/abc/lis-confirmation"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ── GET /{id}/lis-protocol ─────────────────────────────────────

    @Test
    void getActProtocol_returnsProtocolFromService() throws Exception {
        Map<String, Object> protocol = Map.of("conclusion", "Пробы не выявлено");
        when(actService.getProtocol(7L)).thenReturn(protocol);

        mockMvc.perform(get("/v1/act/7/lis-protocol"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conclusion").value("Пробы не выявлено"));

        verify(actService).getProtocol(7L);
    }

    @Test
    void getActProtocol_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/v1/act/abc/lis-protocol"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /{id}/lis-protocol/conclude ────────────────────────────

    @Test
    void concludeActProtocol_delegatesToServiceAndReturnsSuccess() throws Exception {
        ProtocolConclusionRequest request = new ProtocolConclusionRequest("Заключение по протоколу");

        mockMvc.perform(patch("/v1/act/15/lis-protocol/conclude")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("protocol is successfully concluded"));

        ArgumentCaptor<ProtocolConclusionRequest> captor = ArgumentCaptor.forClass(ProtocolConclusionRequest.class);
        verify(actService).concludeProtocol(eq(15L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().conclusion()).isEqualTo("Заключение по протоколу");
    }

    @Test
    void concludeActProtocol_nonNumericId_returns400() throws Exception {
        ProtocolConclusionRequest request = new ProtocolConclusionRequest("Заключение");

        mockMvc.perform(patch("/v1/act/abc/lis-protocol/conclude")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void concludeActProtocol_missingBody_returns400() throws Exception {
        mockMvc.perform(patch("/v1/act/15/lis-protocol/conclude")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
