package uz.uzinfocom.isemid.commons.exceptions;

public class UserBlockedException extends ForbiddenException {

  public UserBlockedException() {}

  public UserBlockedException(final String message) {
    super(message);
  }

  public UserBlockedException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
