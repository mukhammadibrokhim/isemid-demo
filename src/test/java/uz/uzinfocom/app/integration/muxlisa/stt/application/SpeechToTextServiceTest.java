package uz.uzinfocom.app.integration.muxlisa.stt.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;
import uz.uzinfocom.app.integration.muxlisa.common.exception.InvalidAudioException;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaProperties;
import uz.uzinfocom.app.integration.muxlisa.stt.client.MuxlisaSttClient;
import uz.uzinfocom.app.integration.muxlisa.stt.web.dto.SpeechToTextResponse;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpeechToTextServiceTest {

    private MuxlisaSttClient client;
    private SpeechToTextService service;
    private SystemSettingResolver settings;

    @BeforeEach
    void setUp() {
        client = mock(MuxlisaSttClient.class);
        MuxlisaProperties properties = new MuxlisaProperties(
                "https://muxlisa.example", "/api/v2/stt", "key",
                Duration.ofSeconds(1), Duration.ofSeconds(5), DataSize.ofBytes(10), 2);
        settings = mock(SystemSettingResolver.class);
        when(settings.resolveLong(anyString(), anyLong())).thenAnswer(call -> call.getArgument(1));
        service = new SpeechToTextService(client, properties, settings);
    }

    @Test
    void stripsContentTypeParametersAndPicksExtension() {
        when(client.transcribe(any(), any(), any())).thenReturn("matn");

        SpeechToTextResponse response = service.transcribe(new byte[]{1, 2}, "Audio/WebM;codecs=opus");

        assertThat(response.text()).isEqualTo("matn");
        verify(client).transcribe(new byte[]{1, 2}, "audio/webm", "audio.webm");
    }

    @Test
    void maxAudioSizeComesFromRuntimeSetting() {
        when(settings.resolveLong(anyString(), anyLong())).thenReturn(1L);
        when(client.transcribe(any(), any(), any())).thenReturn("matn");

        assertThatThrownBy(() -> service.transcribe(new byte[]{1, 2}, "audio/wav"))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessage("speech.error.audio_too_large");
        verify(client, never()).transcribe(any(), any(), any());
    }

    @Test
    void rejectsEmptyAudio() {
        assertThatThrownBy(() -> service.transcribe(new byte[0], "audio/wav"))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessage("speech.error.empty_audio");
        verify(client, never()).transcribe(any(), any(), any());
    }

    @Test
    void rejectsAudioOverTheConfiguredLimit() {
        assertThatThrownBy(() -> service.transcribe(new byte[11], "audio/wav"))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessage("speech.error.audio_too_large");
        verify(client, never()).transcribe(any(), any(), any());
    }

    @Test
    void rejectsUnsupportedOrMissingContentType() {
        assertThatThrownBy(() -> service.transcribe(new byte[]{1}, "application/json"))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessage("speech.error.unsupported_format");
        assertThatThrownBy(() -> service.transcribe(new byte[]{1}, null))
                .isInstanceOf(InvalidAudioException.class);
        assertThatThrownBy(() -> service.transcribe(new byte[]{1}, "not a type"))
                .isInstanceOf(InvalidAudioException.class);
        verify(client, never()).transcribe(any(), any(), any());
    }
}
