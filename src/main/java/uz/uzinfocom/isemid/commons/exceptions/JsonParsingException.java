package uz.uzinfocom.isemid.commons.exceptions;

public class JsonParsingException extends RuntimeException {

  public JsonParsingException() {}

  public JsonParsingException(final String message) {
    super(message);
  }

  public JsonParsingException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public JsonParsingException(final Throwable cause) {
    super(cause);
  }
}
