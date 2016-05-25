package de.htwsaarland.config;

/**
 * <p>LSConfigException class.</p>
 *
 * @author hbui
 * @version $Id: $Id
 */
public class LSConfigException extends RuntimeException{
	
	public LSConfigException(EnvConfiguration source, String msg){
		super(msg + " [config source: " + (source !=null ? source.toString() : "N/A") +"]" );
	}
	
	/**
	 * <p>Constructor for LSConfigException.</p>
	 *
	 * @param msg a {@link java.lang.String} object.
	 */
	public LSConfigException(String msg){
		super(msg);
	}
	
	public LSConfigException(EnvConfiguration source, String msg, Throwable t){
		super(msg + " ("+t.getMessage()+")" + " [config source: " + (source !=null ? source.toString() : "N/A") +"]",t);
	}
	
	/**
	 * <p>Constructor for LSConfigException.</p>
	 *
	 * @param msg a {@link java.lang.String} object.
	 * @param t a {@link java.lang.Throwable} object.
	 */
	public LSConfigException(String msg, Throwable t){
		super(msg + " ("+t.getMessage()+")",t);
	}

	/**
	 * <p>Constructor for LSConfigException.</p>
	 *
	 * @param t a {@link java.lang.Throwable} object.
	 */
	public LSConfigException(Throwable t){
		super(t);
	}

}
