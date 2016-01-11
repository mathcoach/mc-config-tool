package de.htwsaarland.config.processor;

import de.htwsaarland.config.annotation.NeedConfig;
import de.htwsaarland.config.ConfigEntries;
import de.htwsaarland.config.annotation.NeedConfigs;
import de.htwsaarland.laplus.annotation.macros.MConfigEntry;
import de.htwsaarland.laplus.annotation.macros.MEntry;
import de.htwsaarland.laplus.annotation.macros.MEntryArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
	"de.htwsaarland.laplus.parser.annotation.NeedConfig",
	"de.htwsaarland.laplus.parser.annotation.NeedConfigs",
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
			Set<String> configParam = new TreeSet<>();
			//useIn: {
			//	config-name1 -> {class1 -> description1, class2 -> description2, ...} 
			//	config-name2 -> {class1 -> description1, class3 -> description3, ...}     
			//}
			Map<String, Set<ConfigEntries.ConfigUser> > useIn = new HashMap<>();
			// sugguestValue :{
			//  config-name1 -> [value1, value2, value3],
			//  config-name2 -> [value4, value5]
			//}
			Map<String, Set<String>> suggestValue = new HashMap<>();
			List<String> packages = new ArrayList<>();
			info("Element.length " + roundEnv.getElementsAnnotatedWith(NeedConfigs.class).size());
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfigs.class)) {
				String userName = getElementName(elem);
				updatePackage(packages, userName);
				NeedConfigs param = elem.getAnnotation(NeedConfigs.class);
				NeedConfig[] params = param.value();
				for (NeedConfig p : params){
					generateConfigEntry(p, configParam, useIn, userName, suggestValue);
				}
				
				String msg =  "\t" + (++i) + " " + elem.getSimpleName().toString();
				info(msg);
				info("\t"+useIn.toString());
			}
			for (Element elem : roundEnv.getElementsAnnotatedWith(NeedConfig.class)) {
				String userName = getElementName(elem);
				updatePackage(packages, userName);
				NeedConfig param = elem.getAnnotation(NeedConfig.class);
				generateConfigEntry(param, configParam, useIn, userName, suggestValue);
				String msg =  "\t" + (++i) + " " + elem.getSimpleName().toString();
				info(msg);
				info("\t"+useIn.toString());
			}
			
			String apackage = processingEnv.getOptions().get(CONFIG_PACKAGE) ;
			info("\tConfigurated package name of GenNeedConfigEntry is '"+ apackage + "'.");
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
				for (String  c: configParam){
					MEntry newEntry = entryArraytemplate.newEntry(c);
					for ( ConfigEntries.ConfigUser u : useIn.get(c)){
						newEntry.newUseIn( u.name,u.description );
					}
					for (String s : suggestValue.get(c)){
						newEntry.newSuggestValue(s);
					}
				}
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

	private void generateConfigEntry(
				NeedConfig p, 
				Set<String> configParam, 
				Map<String, Set<ConfigEntries.ConfigUser>> useIn, 
				String userName, 
				Map<String, Set<String>> suggestValue)
	{
		final String configParamName =  StringEscapeUtils.escapeJava( p.name() );
		configParam.add(p.name());
		Set<ConfigEntries.ConfigUser> u = useIn.get(configParamName);
		if (u==null){
			u = new TreeSet<>();
			useIn.put( configParamName, u );
		}
		String d = getDescription(p.description());
		u.add(new ConfigEntries.ConfigUser(userName, d) );
		
		Set<String> s = suggestValue.get(configParamName);
		if (s == null){
			s = new TreeSet<>();
			suggestValue.put(configParamName, s);
		}
		for(String sugguest : p.sugguestValues()){
			if(sugguest.trim().length()>0){
				s.add(sugguest.trim());
			}
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
			String d = String.join("", descriptionArray);
			return StringEscapeUtils.escapeJava(d) ;
		}
	}
}
