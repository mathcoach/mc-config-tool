package de.htwsaar.config.processor;

import de.htwsaar.config.ConfigEntries.Entry;
import de.htwsaar.config.annotation.NeedConfig;
import de.htwsaar.config.annotation.NeedConfigs;
import de.htwsaar.config.macros.MConfigEntry;
import de.htwsaar.config.macros.MEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import de.htwsaar.config.processor.text.StringEscapeUtils;


/**
 *
 * @author hbui
 */
@SupportedAnnotationTypes({
	"de.htwsaar.config.annotation.NeedConfig",
	"de.htwsaar.config.annotation.NeedConfigs",})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(value = NeedConfigGenerator.CONFIG_PACKAGE)
public class NeedConfigGenerator extends MCAbstractAnnotationProcessor {

	public static final String CONFIG_PACKAGE = "config.package";

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		debug("NeedConfigGenerator called");
        
		try {
			int i = 0;
			Map<String, Entry> configParam = new HashMap<>();
			List<String> packages = new ArrayList<>();
			debug("Element.length " + roundEnv.getElementsAnnotatedWith(NeedConfigs.class).size());
			// Process multiple annotations
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfigs.class)) {
				final String userName = getElementName(elem);
				PackageNameHandling.updatePackage(packages, userName, lw);
				NeedConfigs param = elem.getAnnotation(NeedConfigs.class);
				for (NeedConfig p : param.value()) {
					transform(userName, p, configParam);
				}
				String msg = "\t" + (++i) + " " + elem.getSimpleName();
				info(msg);
			}
			// Process simple annotation
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfig.class)) {
				final String userName = getElementName(elem);
				PackageNameHandling.updatePackage(packages, userName, lw);
				NeedConfig param = elem.getAnnotation(NeedConfig.class);
				transform(userName, param, configParam);
			}
            String configuratedPackage = processingEnv.getOptions().get(CONFIG_PACKAGE);
			String finalPackageName = PackageNameHandling.buildPackage(packages, configuratedPackage, lw);
			info("\tFinal package name of GenNeedConfigEntry is '" + finalPackageName + "'.");
			String className = buildUUIDClassName();
			if (!configParam.isEmpty()) {
				MConfigEntry configEntryTemplate = new MConfigEntry(finalPackageName, className);
				configParam.forEach((configParameterName, configEntry) -> {
					MEntry newEntry = configEntryTemplate.newEntry(configParameterName);
					configEntry.useIn().forEach(u
							-> newEntry.newUseIn(
									StringEscapeUtils.escapeJava(u.name),
									StringEscapeUtils.escapeJava(u.description))
					);
					configEntry.suggestValue().forEach(
							s -> newEntry.newSuggestValue(StringEscapeUtils.escapeJava(s))
					);
				});
				writeJavaFileToDisk(configEntryTemplate.toString(), finalPackageName + "." + className);
			}
			info("Generate Configuration Information Finish");
		} catch (Exception ex) {
			error(ex.getMessage());
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (StackTraceElement trace : stackTrace) {
				warn(trace.toString());
			}
		}
		return true;
	}

	private void transform(String userName, NeedConfig annotation, Map<String, Entry> entries) {
		final String configName = annotation.name();
		Entry e = entries.get(configName);
		if (e == null) {
			e = new Entry(configName);
			entries.put(configName, e);
		}
		e.addUseIn(userName, getDescription(annotation.description()));
		for (String s : annotation.sugguestValues()) {
			e.addSuggestValue(s);
		}
	}

	private String getElementName(Element elem) {
		String name = ((TypeElement) elem).getQualifiedName().toString();
		info(name);
		return name;
	}
    
	private static String buildUUIDClassName() {
		return "GenConfigEntry";//NOSONAR for now it works well, but keep it as a method for changing in future
	}

    /** 
     * because default of descript is an empty array, so {@code description = null} is not allowed.
     */
	private static String getDescription(String[] descriptionArray) {
		if (descriptionArray.length == 0) {
			return "";
		} else {
			return String.join("", descriptionArray);
		}
	}
}
