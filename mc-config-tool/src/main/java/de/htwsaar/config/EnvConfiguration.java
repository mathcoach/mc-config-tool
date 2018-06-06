package de.htwsaar.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hbui
 */
public interface EnvConfiguration {

	int MAX_IMPORT_LEVEL = 1;
	String IMPORT_KEY = "import";
	Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

	/**
	 * Gibt den Konfiguration-Wert zurück oder {@code null} wenn die
	 * Konfiguration-Param nicht in dem  eigenen Configurationstabelle existiert.
	 *
	 * @param configParameter a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getConfigValue(String configParameter);

	/**
	 * Gibt den Konfigurations-Wert zurück, oder den Default Wert wenn der
	 * Konfigurations-Parameter nicht im eigene Konfigurationstabelle existiert,
	 *
	 * @param configParameter Der Konfigurationsparameter
	 * @param defaultValue Default Value wenn der Konfigurationsparameter nicht existert
	 * @return Wie in der Beschreibung
	 * */
	default String getConfigValue(String configParameter, String defaultValue) {
		String configValue = getConfigValue(configParameter);
		if (configValue == null) {
			return defaultValue;
		}
		return configValue;
	}

	/**
	 * Gibt den Konfigurations-Wert zurück, oder ruft den gegeben handler auf wenn der Konfig nicht existiert.
	 *
	 * @param configParameter Der Parameter
	 * @param handler Handles missing configuration
	 *
	 * @return as description
	 *
	 * */
	default String getConfigValue(String configParameter, MissingConfigurationHandler handler) {
		String configValue = getConfigValue(configParameter);
		if (configValue == null) {
			return handler.handleMissingConfiguration(configParameter);
		}
		return configValue;
	}

	/**
	 * @return the url, from which the config are saved, or null if unknown!
	 */
	@Override
	String toString();

	Set<String> getAllConfigKeys();

	static Map<String, String> resolveImportConfig(File configFile, ConfigParser parser) {

		Map<String, String> temp = parser.parseConfigFile(configFile);
		int importLevel = 0;
		while (temp.containsKey(IMPORT_KEY)) {
			++importLevel;
			if (importLevel > MAX_IMPORT_LEVEL) {
				throw new LSConfigException("Import too many levels " + configFile.getAbsolutePath());
			} else {
				parser.reset();
				Path importedPath = Paths.get(resolveSystemProperties(temp.get(IMPORT_KEY))) //NOSONAR
						.toAbsolutePath().normalize();
				// Not need to check if file exists it make the code in ConfigParser.parseConfigFile
				temp.remove(IMPORT_KEY);
				Map<String, String> importedConfig = parser.parseConfigFile(importedPath.toFile());
				temp.putAll(importedConfig);
				
				/*
				File importedFile = new File(resolveSystemProperties(temp.get(IMPORT_KEY)));
				temp.remove(IMPORT_KEY);
				Map<String, String> importedConfig = parser.parseConfigFile(importedFile);
				temp.putAll(importedConfig);
				 */
			}
		}
		return temp;

	}

	static Map<String, String> resolveConfigVariables(final Map<String, String> originConfigTable) {
		final Logger logger = LoggerFactory.getLogger(EnvConfiguration.class);
		try {
			Map<String, String> envVar = new HashMap<String, String>(8) {
				{
					put("HOME", System.getProperty("user.home"));
				}
			};
			HashMap<String, String> configTable = new HashMap();
			originConfigTable.entrySet().stream().forEach(entry -> {
				String k = entry.getKey();
				String v = StrSubstitutor.replace(entry.getValue(), originConfigTable);
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
		return StrSubstitutor.replaceSystemProperties(resolvedText);
		//return resolvedText;
	}

	static void setConfigValue(
			final String configParameter,
			final String newValue,
			final Map<String, String> configTable
	) {
		Matcher matcher = VAR_PATTERN.matcher(newValue);
		if (matcher.find()) {// If the value has a variable
			StrBuilder b = new StrBuilder(newValue);
			StrSubstitutor st = new StrSubstitutor(configTable);
			boolean substable = st.replaceIn(b);
			if (substable) {
				String resolvedValue = b.build();
				Matcher afterSubstMatcher = VAR_PATTERN.matcher(resolvedValue);
				if (afterSubstMatcher.find()) {//If there is one or more not resolveable variables
					throw new LSConfigException("Cannot find the variable '" + afterSubstMatcher.group(0) + "in '" + newValue + "'");
				} else {
					configTable.put(configParameter, resolvedValue);
				}
			} else {// nothing is replaced!!!
				throw new LSConfigException("Cannot find any variables in " + newValue);
			}
		} else {
			configTable.put(configParameter, newValue);
		}
	}

	@FunctionalInterface
	interface MissingConfigurationHandler {
		String handleMissingConfiguration(String configurationParameter);
	}
}
