package group.dump.exception;

/**
 * @author yuanguangxin
 */
public class DumpException extends RuntimeException {

    public DumpException(String msg) {
        super(msg);
    }

    public DumpException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
