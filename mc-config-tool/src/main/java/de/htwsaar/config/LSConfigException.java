package de.htwsaar.config;

/**
 * <p>
 * LSConfigException class.</p>
 *
 * @author hbui
 * @version $Id: $Id
 */
public class LSConfigException extends RuntimeException {

    public LSConfigException(EnvConfiguration source, String msg) {
        super(msg + " [config source: " + source + "]");
    }

    /**
     * <p>
     * Construct a LSConfigException with an error message<p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public LSConfigException(String msg) {
        super(msg);
    }

    /**
     * construct a LSConfigException with a error message, the cause and the
     * source of config.
     *
     * @param source the configuration source
     * @param msg the error message to be shown
     * @param cause the cause.
     */
    public LSConfigException(EnvConfiguration source, String msg, Throwable cause) {
        super(msg + " (" + cause + ")" + " [config source: " + source + "]", cause);
    }

    /**
     * <p>
     * Constructor for LSConfigException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public LSConfigException(String msg, Throwable cause) {
        super(msg + " (cause: " + cause.getMessage() + ")", cause);
    }

    /**
     * <p>
     * Constructor for LSConfigException.</p>
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    public LSConfigException(Throwable t) {
        super(t);
    }

}
