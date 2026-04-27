package uz.uzinfocom.isemid.commons.exceptions;

public class InvalidDBCommandException extends RuntimeException {

  public InvalidDBCommandException() {}

  public InvalidDBCommandException(final String message) {
    super(message);
  }

  public InvalidDBCommandException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidDBCommandException(final Throwable cause) {
    super(cause);
  }
}
