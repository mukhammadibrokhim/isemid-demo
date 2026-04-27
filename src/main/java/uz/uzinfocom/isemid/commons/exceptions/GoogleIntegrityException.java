package uz.uzinfocom.isemid.commons.exceptions;

public class GoogleIntegrityException extends RuntimeException {

  public GoogleIntegrityException() {}

  public GoogleIntegrityException(final String message) {
    super(message);
  }

  public GoogleIntegrityException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public GoogleIntegrityException(final Throwable cause) {
    super(cause);
  }

  public GoogleIntegrityException(
      final String message,
      final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
