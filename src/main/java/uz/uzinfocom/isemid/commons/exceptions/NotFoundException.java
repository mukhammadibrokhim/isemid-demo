package uz.uzinfocom.isemid.commons.exceptions;

import java.util.NoSuchElementException;

public class NotFoundException extends NoSuchElementException {

  public NotFoundException() {}

  public NotFoundException(final String message) {
    super(message);
  }

  public NotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public NotFoundException(final Throwable cause) {
    super(cause);
  }
}
