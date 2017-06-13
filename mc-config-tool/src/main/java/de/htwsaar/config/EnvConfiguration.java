package de.htwsaar.config;

import java.util.Set;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hbui
 */
public interface EnvConfiguration {

	Logger LOGGER = LoggerFactory.getLogger(EnvConfiguration.class);
	int MAX_IMPORT_LEVEL = 1;

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

	static String resolveSystemProperties(final String text) {
		String resolvedText = text.replace("$HOME", "${user.home}")
			.replace("${HOME}", "${user.home}")
			.replace("$PWD", "${user.dir}")
			.replace("${PWD}", "${user.dir}");
		resolvedText = StrSubstitutor.replaceSystemProperties(resolvedText);
		return resolvedText;
	}
}
