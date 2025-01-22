package uz.uzinfocom.isemid.exceptioins;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

