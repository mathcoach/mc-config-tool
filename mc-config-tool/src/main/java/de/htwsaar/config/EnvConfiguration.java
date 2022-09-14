package de.htwsaar.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BiFunction;

import de.htwsaar.config.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a map of configuration key-value.
 *
 * @author hbui
 *
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
	 * returns the value of a configure parameter, or return
     * {@code handler.handleMissingConfiguration()} if configure value is null.
     * @see #getConfigValue(java.lang.String, java.util.function.BiFunction)
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
     * Calls {@code converter.apply(configParameter, configValue)} to convert
     * the configuration value as string to expected value and type. Example;
     *
     * <pre><code>
     * int poolCount = config.getConfigValue("database.poolCount", (String key, String value, int) -> {
     *     try {
     *         return Integer.parseInt(value);
     *     } catch(NumberFormatException ex)  {
     *         return 1;
     *     }
     * } );
     * </code></pre>
     *
     * @param configParameter the configure parameter
     * @param converter the method calls
     * {@code converter.apply(configParameter, configValue);} to calculate the
     * returned result. Lambda Function {@code converter.apply} must be able to
     * handle null-value of configValue.
     *
     * @return result of {@code converter.apply(configParameter, configValue);}
     */
    default <R> R getConfigValue(String configParameter, BiFunction<String, String, R> converter) {
        String configValue = getConfigValue(configParameter);
        return converter.apply(configParameter, configValue);
    }

    /**
     * should return the URL, from which the configuration saved, or null if
     * unknown. Use for Debug only
     *
	 * @return the URL, from which the config are saved, or null if unknown!
	 */
	@Override
	String toString();

	Set<String> getAllConfigKeys();


    /**
     * resolve an config-file, which is imported by an other configure-file.
     * This method is intended to be used by an implementation of {@link EnvConfiguration}.
     *
     * @param configFile the imported configure-file
     * @param parser a parser, which can parse the configure file.
     *
     * @return configure as a Map of paar of configure-parameter and its value
     *
     */
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
				if(importedPath.toFile().isFile() ) {
					temp.remove(IMPORT_KEY);
					Map<String, String> importedConfig = parser.parseConfigFile(importedPath.toFile());
					temp.putAll(importedConfig);
				}else {
					throw new ImportCfgFileNotFound(importedPath);
				}
			}
		}
		return temp;

	}

    /**
     * resolve variables in configure values of a configure-parameter. Variables
     * are marked by <code>${var_name}</code>. <code>var_name</code> should be
     * consist of only numeric-alphabetical character, the minus and underscore
     * character. The special variable <code>HOME</code> is automatically resolved
     * to value of Java-System variable <code>user.home</code>.
     *
     * For example: given a Map of pair of configure parameters
     * and their values:
     *
     * <pre>
     * <code>
     * application-home=${HOME}/some-dir
     * icons-path=${application-home}/icons
     * style-path=${application-home}/style
     * </code></pre>
     * within a java Runtime with <code>user.home</code> is set to <code>/home/otto</code>.
     * This map is resolved to
     * <pre><code>
     * application-home=/home/otto/some-dir
     * icons-path=/home/otto/some-dir/icons
     * style-path=/home/otto/some-dir/style
     * </code></pre>
     *
     * the origin map is not changed.
     *
     * @param originConfigTable
     * @return new map, its values are resolved.
     * @throws LSConfigException when at least a variable is not resolved.
     */
	static Map<String, String> resolveConfigVariables(final Map<String, String> originConfigTable) {
		final Logger logger = LoggerFactory.getLogger(EnvConfiguration.class);
		try {
            Map<String, String> envVar = Map.of(
                "HOME", System.getProperty("user.home"),
                "PWD", System.getProperty("user.dir")
            );
			HashMap<String, String> configTable = new HashMap<>();
			originConfigTable.entrySet().stream().forEach(entry -> {
				String k = entry.getKey();
				String v = StringSubstitutor.replace(entry.getValue(), originConfigTable);
				v = StringSubstitutor.replaceSystemProperties(v);
				v = StringSubstitutor.replace(v, envVar);
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
		return StringSubstitutor.replaceSystemProperties(resolvedText);
	}

	static void setConfigValue(
			final String configParameter,
			final String newValue,
			final Map<String, String> configTable
	) {
		Matcher matcher = VAR_PATTERN.matcher(newValue);
		if (matcher.find()) {// If the value has a variable
            StringBuilder bufferedValue = new StringBuilder(newValue);
			StringSubstitutor substitutor = new StringSubstitutor(configTable);
			boolean substable = substitutor.replaceIn(bufferedValue);
			if (substable) {
				String resolvedValue = bufferedValue.toString();
				Matcher afterSubstMatcher = VAR_PATTERN.matcher(resolvedValue);
				if (afterSubstMatcher.find()) {//If there is at least one not resolveable variables
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
