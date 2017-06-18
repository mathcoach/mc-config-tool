package de.htwsaar.config.processor;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author hbui
 */
public abstract class MCAbstractAnnotationProcessor extends AbstractProcessor{
	protected static final String PACKAGE_REG_VALIDATOR = 
		"([a-zA-Z]+[a-zA-Z0-9]*)(\\.[a-zA-Z]+[a-zA-Z0-9]*)*";
	private Messager messager;
	private LogWriter lw;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv); 
		messager = processingEnv.getMessager();
		lw = getLogWriter();
	}
	
	protected final void error(CharSequence error){
		lw.error(error);
	}
	
	protected final void warn(CharSequence warn){
		lw.warn(warn);
	}

	protected final void info(CharSequence info){
		lw.info(info);
	}
	
	protected final void debug(CharSequence info){
		lw.debug(info);
	}
	
	
	
	/**
	 * @param code the content of the java file
	 * @param name the full qualified java file name (domain.package.ClassName).
	 */
	protected final void writeJavaFileToDisk(String code, String name) {
		try {
			JavaFileObject jfo = processingEnv.getFiler().createSourceFile(name);
			info("creating source file: " + jfo.toUri());
			try (Writer writer = jfo.openWriter()) {
				writer.write(code);
				writer.flush();
			}
		} catch (IOException ex) {
			error("fail to write class " + name + " to disk! " + ex.getMessage());
		}
	}

	protected final boolean validePackageName(String packageName){
		return packageName.matches(PACKAGE_REG_VALIDATOR);
	}

	protected final LogWriter getLogWriter(){
		return new LogWriter() {
			@Override
			public void error(CharSequence msg) {
				messager.printMessage(Diagnostic.Kind.ERROR, msg);
			}

			@Override
			public void warn(CharSequence msg) {
				messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
			}

			@Override
			public void info(CharSequence msg) {
				messager.printMessage(Diagnostic.Kind.NOTE , msg);
			}

			@Override
			public void debug(CharSequence msg) {
				messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
			}
		};
	}
}


