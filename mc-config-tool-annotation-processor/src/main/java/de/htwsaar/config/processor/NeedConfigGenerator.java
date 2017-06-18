package de.htwsaar.config.processor;

import de.htwsaar.config.ConfigEntries.Entry;
import de.htwsaar.config.annotation.NeedConfig;
import de.htwsaar.config.annotation.NeedConfigs;
import de.htwsaar.config.macros.MConfigEntry;
import de.htwsaar.config.macros.MEntry;
import de.htwsaar.config.macros.MEntryArray;
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
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author hbui
 */
@SupportedAnnotationTypes({
	"de.htwsaar.config.annotation.NeedConfig",
	"de.htwsaar.config.annotation.NeedConfigs",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(value = NeedConfigGenerator.CONFIG_PACKAGE)
public class NeedConfigGenerator  extends MCAbstractAnnotationProcessor{
	
	public static final String CONFIG_PACKAGE = "config.package";

	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		info("NeedConfigGenerator called");
		try{
			int i = 0;
			Map<String, Entry> configParam = new HashMap<>();
			List<String> packages = new ArrayList<>();
			info("Element.length " + roundEnv.getElementsAnnotatedWith(NeedConfigs.class).size());
			// Process multiple annotations
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfigs.class)) {
				final String userName = getElementName(elem);
				updatePackage(packages, userName);
				NeedConfigs param = elem.getAnnotation(NeedConfigs.class);
				/*NeedConfig[] params = param.value();*/
				for (NeedConfig p : param.value() ){
					transform(userName, p, configParam);
				}
				String msg =  "\t" + (++i) + " " + elem.getSimpleName().toString();
				info(msg);
			}
			// Process simple annotation
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfig.class)) {
				final String userName = getElementName(elem);
				updatePackage(packages, userName);
				NeedConfig param = elem.getAnnotation(NeedConfig.class);
				transform(userName, param, configParam);
			}
			//Prepair java class and package information for configuration
			String apackage = processingEnv.getOptions().get(CONFIG_PACKAGE) ;
			info("\tConfigurated package name of GenNeedConfigEntry is " + (apackage==null?"null.":"'"+apackage+"'."));
			if(apackage == null || apackage.trim().length()== 0 ){
				warn("\tCannot use given package name of GenNeedConfigEntry '"
							+ apackage + "'.");
				apackage = String.join(".", packages.toArray(new String[packages.size()])) ;
			}else{
				if (!validePackageName(apackage)){
					warn("\t"+apackage + " is not a valide Java Package");
					apackage = String.join(".", packages.toArray(new String[packages.size()])) ;
				}
			}
			warn("\tFinal package name of GenNeedConfigEntry is '" + apackage + "'.");
			String className = getUUIDClassName();
			if (! configParam.isEmpty() ){
				MConfigEntry configEntryTemplate = new MConfigEntry(apackage, className);
				MEntryArray entryArraytemplate = configEntryTemplate.newEntryArray();
				configParam.forEach( (configParameterName, configEntry) -> {
					MEntry newEntry = entryArraytemplate.newEntry(configParameterName);
					configEntry.useIn().forEach( u -> 
						newEntry.newUseIn(
								StringEscapeUtils.escapeJava(u.name), 
								StringEscapeUtils.escapeJava(u.description) )
					);
					configEntry.suggestValue().forEach(
							s -> newEntry.newSuggestValue (StringEscapeUtils.escapeJava(s)) 
					);
				});
				writeJavaFileToDisk(configEntryTemplate.toString(), apackage +"." + className);
			}
			info("Generate Configuration Information Finish");
		}catch(Exception ex){
			error(ex.getMessage());
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for(StackTraceElement trace: stackTrace){
				warn(trace.toString());
			}
		}
		return true;
	}
	
	private void transform(String userName, NeedConfig annotation, Map<String,Entry> entries){
		final String configName = annotation.name();
		Entry e = entries.get(configName);
		if (e == null){
			e = new Entry() {
				@Override
				public String getName() {
					return configName;
				}
			};
			entries.put(configName, e);
		}
		e.addUseIn(userName, getDescription(annotation.description()) );
		for(String s: annotation.sugguestValues() ){
			e.addSuggestValue(s);
		}
	}
	
	private String getElementName(Element elem){
		if (elem instanceof TypeElement){
			String name = ((TypeElement)elem).getQualifiedName().toString();
			info(name);
			return name;
		}else{
			return elem.getSimpleName().toString();
		}
	}

	private void updatePackage(List<String> packages,String name){
		String[] split = name.split("\\.");
		info("split.length " + split.length);
		if (packages.isEmpty()){
			info("package is empty");
			for(int i = 0; i < split.length-1; i++){
				info("add " + split[i] + " to package");
				packages.add(split[i]);
			}
		}else{
			int i ;
			int lastIdx = split.length-1 < packages.size() 
					? split.length-1 
					: packages.size();
			for (i = 0; i < lastIdx; ++i){
				String p = packages.get(i);
				info("compare " + p + " to " + split[i]);
				if (!p.equals(split[i])){
					break;
				}
			}
			for(int j = i; j < packages.size(); j++){
				info("drop " + packages.get(i));
				packages.remove(i);
			}
		}
	}

	private static String getUUIDClassName() {
		return "GenConfigEntry";
	}

	private static String getDescription(String[] descriptionArray){
		if (descriptionArray==null||descriptionArray.length==0){
			return "";
		}else{
			return String.join("", descriptionArray);
		}
	}
}
