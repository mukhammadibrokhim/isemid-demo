package uz.uzinfocom.isemid.commons.exceptions;

public class ProtocolNotReadyException extends RuntimeException {

  public ProtocolNotReadyException() {}

  public ProtocolNotReadyException(final String message) {
    super(message);
  }

  public ProtocolNotReadyException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ProtocolNotReadyException(final Throwable cause) {
    super(cause);
  }
}
