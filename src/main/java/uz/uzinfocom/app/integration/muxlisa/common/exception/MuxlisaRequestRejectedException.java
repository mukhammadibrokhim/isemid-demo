package uz.uzinfocom.app.integration.muxlisa.common.exception;

import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Muxlisa rejected the audio itself (400/413/415/422, e.g. unsupported format or
 * too long). Caller-side, so it is ignored by the {@code muxlisa} circuit breaker.
 */
public class MuxlisaRequestRejectedException extends MuxlisaIntegrationException {

    public MuxlisaRequestRejectedException() {
        super(ErrorCode.BAD_REQUEST, "muxlisa.error.rejected");
    }

    private MuxlisaRequestRejectedException(String messageCode) {
        super(ErrorCode.BAD_REQUEST, messageCode);
    }

    /**
     * Picks the most helpful message from Muxlisa's own explanation. Muxlisa sniffs the
     * file's bytes, so e.g. a browser webm recording is refused as "video/webm" even
     * though the Content-Type we sent was audio/webm - the caller should send WAV.
     */
    public static MuxlisaRequestRejectedException fromReason(String reason) {
        if (reason != null && reason.toLowerCase(java.util.Locale.ROOT).contains("invalid audio type")) {
            return new MuxlisaRequestRejectedException("muxlisa.error.invalid_audio_type");
        }
        return new MuxlisaRequestRejectedException();
    }
}
