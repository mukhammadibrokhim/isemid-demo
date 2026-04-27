package uz.uzinfocom.isemid.commons.exceptions;

public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {}

  public InvalidCredentialsException(final String message) {
    super(message);
  }

  public InvalidCredentialsException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidCredentialsException(final Throwable cause) {
    super(cause);
  }
}
