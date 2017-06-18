package de.htwsaar.config.processor;


public interface LogWriter {
	void error(CharSequence msg);
	void warn(CharSequence msg);
	void info(CharSequence msg);
	void debug(CharSequence msg);
}