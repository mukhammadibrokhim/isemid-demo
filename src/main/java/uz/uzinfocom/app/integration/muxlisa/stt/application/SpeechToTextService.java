package uz.uzinfocom.app.integration.muxlisa.stt.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaProperties;
import uz.uzinfocom.app.integration.muxlisa.stt.client.MuxlisaSttClient;
import uz.uzinfocom.app.integration.muxlisa.stt.web.dto.SpeechToTextResponse;
import uz.uzinfocom.app.integration.muxlisa.common.exception.InvalidAudioException;

import java.util.Locale;
import java.util.Map;

/**
 * Validates a recording before it leaves our server (non-empty, within Muxlisa's
 * size limit, a format Muxlisa accepts) and hands it to {@link MuxlisaSttClient}.
 * Muxlisa also caps the audio at 60 seconds; that is enforced by Muxlisa itself
 * (it answers 400/422, surfaced as a rejected request) since the duration cannot
 * be read reliably across all the accepted containers without decoding them.
 */
@Service
@RequiredArgsConstructor
public class SpeechToTextService {

    /** Muxlisa-supported MIME subtypes -> file extension sent along with the upload. */
    private static final Map<String, String> EXTENSIONS = Map.ofEntries(
            Map.entry("audio/mpeg", "mp3"),
            Map.entry("audio/x-wav", "wav"),
            Map.entry("audio/vnd.wav", "wav"),
            Map.entry("audio/wav", "wav"),
            Map.entry("audio/wave", "wav"),
            Map.entry("audio/ogg", "ogg"),
            Map.entry("audio/flac", "flac"),
            Map.entry("audio/x-m4a", "m4a"),
            Map.entry("audio/aac", "aac"),
            Map.entry("audio/mp4", "mp4"),
            Map.entry("video/mp4", "mp4"),
            Map.entry("audio/webm", "webm"),
            Map.entry("video/webm", "webm"),
            Map.entry("audio/3gpp", "3gp"),
            Map.entry("audio/3gpp2", "3g2"),
            Map.entry("audio/x-ms-wma", "wma"),
            Map.entry("audio/amr", "amr")
    );

    private final MuxlisaSttClient client;
    private final MuxlisaProperties properties;

    public SpeechToTextResponse transcribe(byte[] audio, String rawContentType) {
        if (audio == null || audio.length == 0) {
            throw new InvalidAudioException("speech.error.empty_audio");
        }
        if (audio.length > properties.maxAudioSize().toBytes()) {
            throw new InvalidAudioException("speech.error.audio_too_large", properties.maxAudioSize().toMegabytes());
        }

        String contentType = normalize(rawContentType);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new InvalidAudioException("speech.error.unsupported_format");
        }

        return new SpeechToTextResponse(client.transcribe(audio, contentType, "audio." + extension));
    }

    /** {@code audio/webm;codecs=opus} -> {@code audio/webm}; anything unparsable -> empty (rejected). */
    private static String normalize(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            return "";
        }
        try {
            MediaType type = MediaType.parseMediaType(rawContentType);
            return (type.getType() + "/" + type.getSubtype()).toLowerCase(Locale.ROOT);
        } catch (InvalidMediaTypeException exception) {
            return "";
        }
    }
}
