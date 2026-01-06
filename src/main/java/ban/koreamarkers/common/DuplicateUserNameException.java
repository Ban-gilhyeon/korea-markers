package ban.koreamarkers.common;

public class DuplicateUserNameException extends RuntimeException{
    public DuplicateUserNameException(String message) {
        super(message);
    }
}
