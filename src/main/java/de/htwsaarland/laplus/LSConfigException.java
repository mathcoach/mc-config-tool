/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.htwsaarland.laplus;

/**
 * <p>LSConfigException class.</p>
 *
 * @author hbui
 * @version $Id: $Id
 */
public class LSConfigException extends RuntimeException{
	/**
	 * <p>Constructor for LSConfigException.</p>
	 *
	 * @param msg a {@link java.lang.String} object.
	 */
	public LSConfigException(String msg){
		super(msg);
	}

	/**
	 * <p>Constructor for LSConfigException.</p>
	 *
	 * @param msg a {@link java.lang.String} object.
	 * @param t a {@link java.lang.Throwable} object.
	 */
	public LSConfigException(String msg, Throwable t){
		super(msg,t);
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
