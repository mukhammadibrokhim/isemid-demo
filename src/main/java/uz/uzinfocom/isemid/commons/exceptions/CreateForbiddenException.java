package uz.uzinfocom.isemid.commons.exceptions;

public class CreateForbiddenException extends ForbiddenException {

  public CreateForbiddenException() {}

  public CreateForbiddenException(final String message) {
    super(message);
  }

  public CreateForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public CreateForbiddenException(final Throwable cause) {
    super(cause);
  }
}
