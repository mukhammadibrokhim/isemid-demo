package uz.uzinfocom.app.platform.settings.application.command;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingResponse;
import uz.uzinfocom.app.platform.settings.application.query.mapper.SystemSettingMapper;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;
import uz.uzinfocom.app.platform.settings.repository.SystemSettingRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingCommandServiceTest {

    private final SystemSettingRepository systemSettingRepository = mock(SystemSettingRepository.class);
    private final SystemSettingMapper systemSettingMapper = mock(SystemSettingMapper.class);
    private final AuditResolver auditResolver = mock(AuditResolver.class);

    private final SystemSettingCommandService service =
            new SystemSettingCommandService(systemSettingRepository, systemSettingMapper, auditResolver);

    @Test
    void createRejectsDuplicateKey() {
        when(systemSettingRepository.existsBySettingKeyIgnoreCase("form058.allowed-sources")).thenReturn(true);

        SystemSettingCreateRequest request = new SystemSettingCreateRequest(
                "form058.allowed-sources", "QR,MANUAL", SystemSettingValueType.STRING, "desc", true
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class);

        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void createSavesNewSettingAndDefaultsActiveToTrueWhenNull() {
        when(systemSettingRepository.existsBySettingKeyIgnoreCase("form058.allowed-sources")).thenReturn(false);
        when(systemSettingRepository.save(any(SystemSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(auditResolver.resolve(any())).thenReturn(mock(AuditResponse.class));
        when(systemSettingMapper.toResponse(any(), any())).thenReturn(mock(SystemSettingResponse.class));

        SystemSettingCreateRequest request = new SystemSettingCreateRequest(
                "form058.allowed-sources", "QR,MANUAL", SystemSettingValueType.STRING, "desc", null
        );

        service.create(request);

        org.mockito.ArgumentCaptor<SystemSetting> captor = org.mockito.ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(captor.capture());
        assertThat(captor.getValue().getSettingKey()).isEqualTo("form058.allowed-sources");
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void updateRejectsWhenSettingIsDeleted() {
        SystemSetting deleted = new SystemSetting();
        deleted.setId(1L);
        deleted.setDeleted(true);
        when(systemSettingRepository.findById(1L)).thenReturn(Optional.of(deleted));

        SystemSettingUpdateRequest request =
                new SystemSettingUpdateRequest("value", SystemSettingValueType.STRING, "desc", true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateThrowsNotFoundForUnknownId() {
        when(systemSettingRepository.findById(404L)).thenReturn(Optional.empty());

        SystemSettingUpdateRequest request =
                new SystemSettingUpdateRequest("value", SystemSettingValueType.STRING, "desc", true);

        assertThatThrownBy(() -> service.update(404L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteSoftDeletesSetting() {
        SystemSetting setting = new SystemSetting();
        setting.setId(5L);
        setting.setDeleted(false);
        when(systemSettingRepository.findById(5L)).thenReturn(Optional.of(setting));

        service.delete(5L);

        assertThat(setting.isDeleted()).isTrue();
        verify(systemSettingRepository).save(setting);
    }

    @Test
    void restoreClearsDeletedFlag() {
        SystemSetting setting = new SystemSetting();
        setting.setId(6L);
        setting.setDeleted(true);
        when(systemSettingRepository.findById(6L)).thenReturn(Optional.of(setting));

        service.restore(6L);

        assertThat(setting.isDeleted()).isFalse();
        verify(systemSettingRepository).save(setting);
    }
}
