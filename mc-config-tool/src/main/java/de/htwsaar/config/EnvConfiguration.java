package de.htwsaar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hbui
 */
public interface EnvConfiguration {

	static final int MAX_IMPORT_LEVEL = 1;
	static final String IMPORT_KEY = "import";
	
	/**
	 * Gibt den Konfiguration-Wert zurück oder {@code null} wenn die
	 * Konfiguration-Param nicht in dem {@link #configTable} existiert.
	 *
	 * @param configParameter a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getConfigValue(String configParameter);
	/**
	 * @return the url, from which the config are saved, or null if unknown!
	 */
	@Override
	String toString();
	Set<String> getAllConfigKeys();

	public static Map<String,String> resolveImportConfig(File configFile, XMLConfigParser parser){
		final Logger logger = LoggerFactory.getLogger(EnvConfiguration.class);
		try {
			//XMLConfigParser parser = new XMLConfigParser();
			Map<String, String> temp = parser.parseConfigFile(configFile);
			int importLevel = 0;
			while(temp.containsKey(IMPORT_KEY)){
				if(importLevel > MAX_IMPORT_LEVEL){
					throw new LSConfigException("Import too many levels " + configFile.getAbsolutePath());
				}else{
					++importLevel;
					parser.reset();
					File importedFile = new File(resolveSystemProperties(temp.get(IMPORT_KEY)));
					temp.remove(IMPORT_KEY);
					Map<String,String> importedConfig = parser.parseConfigFile(importedFile);
					temp.putAll(importedConfig);
				}
			}
			
			Map<String, String> envVar = new HashMap<String, String>(8) {
				{
					put("HOME", System.getProperty("user.home"));
				}
			};
			HashMap<String,String> configTable = new HashMap();
			temp.entrySet().stream().forEach(entry -> {
				String k = entry.getKey();
				String v = StrSubstitutor.replace(entry.getValue(), temp);
				v = StrSubstitutor.replaceSystemProperties(v);
				v = StrSubstitutor.replace(v, envVar);
				logger.trace("{} -> {}", k, v);
				configTable.put(k, v);
			});
			return configTable;
		} catch (IllegalStateException ex) {
			throw new LSConfigException(ex.getMessage(), ex);
		}
	}
	
	
	static String resolveSystemProperties(final String text) {
		String resolvedText = text.replace("$HOME", "${user.home}")
			.replace("${HOME}", "${user.home}")
			.replace("$PWD", "${user.dir}")
			.replace("${PWD}", "${user.dir}");
		resolvedText = StrSubstitutor.replaceSystemProperties(resolvedText);
		return resolvedText;
	}
}
