package uz.uzinfocom.isemid.features.user.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.RequestUrlUtils;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms that GET /v1/user/all query params actually bind into UserFilterRequest
 * the way UserSpecification expects them, through the real controller + real
 * @ModelAttribute binding (not just the isolated specification/data-binder checks).
 */
class UserControllerFilterBindingTest {

    private MockMvc mockMvc;
    private final UserService userService = mock(UserService.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final MessageUtil messageUtil = mock(MessageUtil.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService, userMapper, messageUtil))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        when(messageUtil.get(anyString())).thenReturn("OK");
        when(messageUtil.get(anyString(), any())).thenReturn("OK");

        User currentUser = User.builder().id(1L).nnuzb("admin").active(true).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userService.filter(any(UserFilterRequest.class), any(User.class))).thenReturn(emptyPage);

        RequestUrlUtils.setPublicBaseUrl("http://localhost");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void organizationAndRoleFilters_bindFromQueryParams() throws Exception {
        mockMvc.perform(get("/v1/user/all")
                        .param("organizationStateCode", "UZ-AN")
                        .param("organizationCityCode", "AN-202")
                        .param("userOrgMedicalTypes", "HOSPITAL", "PHARMACY")
                        .param("roleIds", "1", "2"))
                .andExpect(status().isOk());

        ArgumentCaptor<UserFilterRequest> captor = ArgumentCaptor.forClass(UserFilterRequest.class);
        verify(userService).filter(captor.capture(), any(User.class));

        UserFilterRequest bound = captor.getValue();
        System.out.println("Bound filter: stateCode=" + bound.getOrganizationStateCode()
                + " cityCode=" + bound.getOrganizationCityCode()
                + " medicalTypes=" + bound.getUserOrgMedicalTypes()
                + " roleIds=" + bound.getRoleIds());

        assertThat(bound.getOrganizationStateCode()).isEqualTo("UZ-AN");
        assertThat(bound.getOrganizationCityCode()).isEqualTo("AN-202");
        assertThat(bound.getUserOrgMedicalTypes()).containsExactlyInAnyOrder(MedicalType.HOSPITAL, MedicalType.PHARMACY);
        assertThat(bound.getRoleIds()).containsExactlyInAnyOrder(1L, 2L);
    }
}
