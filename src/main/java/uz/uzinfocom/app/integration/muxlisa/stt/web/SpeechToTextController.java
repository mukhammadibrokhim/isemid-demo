package uz.uzinfocom.app.integration.muxlisa.stt.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.muxlisa.stt.application.SpeechToTextService;
import uz.uzinfocom.app.integration.muxlisa.stt.web.dto.SpeechToTextResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(name = "Speech to text", description = "Голосовой ввод: распознавание речи через Muxlisa AI.")
@RestController
@RequestMapping(ApiPaths.Speech.ROOT)
@RequiredArgsConstructor
public class SpeechToTextController {

    private final SpeechToTextService service;

    @Operation(
            summary = "Распознать речь в текст",
            description = "Тело запроса — сырой аудиофайл (не multipart), заголовок Content-Type — MIME-тип записи "
                    + "(audio/wav рекомендуется; также webm, ogg, mp4, mpeg, flac, aac, amr и др.). "
                    + "Максимум 5 МБ и 60 секунд. Аудио нигде не сохраняется. "
                    + "Доступно ролям с правом SPEECH_TO_TEXT / CREATE."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_SPEECH_TO_TEXT_CREATE')")
    public SpeechToTextResponse transcribe(
            @Parameter(description = "Аудиозапись (raw bytes).",
                    content = @Content(mediaType = "audio/*", schema = @Schema(type = "string", format = "binary")))
            @RequestBody byte[] audio,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType
    ) {
        return service.transcribe(audio, contentType);
    }
}
