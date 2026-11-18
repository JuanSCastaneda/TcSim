package TcSimExceptions;

public class NameParsingError extends RuntimeException {
    public NameParsingError(String message) {
        super(message);
    }
}
