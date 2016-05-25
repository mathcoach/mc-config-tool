package de.htwsaarland.config;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

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

	/**
	 * <p>
	 * parseXMLConfigFile.</p>
	 *
	 * @param configFile a {@link java.io.File} object.
	 * @param configTable a {@link java.util.Map} object.
	 */
	static void parseXMLConfigFile(
		final File configFile,
		final Map<String, String> configTable,
		int importedLevel) {

		try {
			if (importedLevel > MAX_IMPORT_LEVEL) {
				throw new LSConfigException("Import to many levels " + configFile.getAbsolutePath());
			}
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new ConfigParser(configTable, importedLevel));
			xmlReader.parse(configFile.toURI().getPath());
			if (LOGGER.isTraceEnabled()) {
				Set<Map.Entry<String, String>> entrySet = configTable.entrySet();
				for (Map.Entry<String, String> config : entrySet) {
					LOGGER.trace("{} -> {}", config.getKey(), config.getValue());
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			String fileName = "null";
			if (configFile != null) {
				Path p = configFile.toPath().normalize();
				fileName = p.getFileName().normalize().toString();
			}
			LOGGER.error("Cannot parse config file: {} ({})",
				fileName, ex.getMessage());
			LOGGER.trace("{}", ex);
			throw new LSConfigException("Error by parsing file " + fileName, ex);
		}catch(NullPointerException ex){
			throw new LSConfigException("Connfig file is null", ex);
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

	class ConfigParser extends DefaultHandler {

		private final Map<String, String> configMap;
		private final StringBuilder value;
		private final int importedLevel;

		public ConfigParser(Map<String, String> configMap, int importedLevel) {
			Preconditions.checkNotNull(configMap, "Argument configMap must not be null");
			this.configMap = configMap;
			value = new StringBuilder(100);
			this.importedLevel = importedLevel;
		}

		@Override
		public void startElement(String uri,
			String localName,
			String qName,
			Attributes attributes)
			throws SAXException {
			LOGGER.trace("Start element '{}'", localName);
			if ("configuration".equals(qName)) {
				String importAtt = attributes.getValue("import");
				if (importAtt != null) {
					String importedPath = resolveSystemProperties(importAtt);
					File importedFile = new File(importedPath);
					LOGGER.info("Parse config file {}", importedFile.getAbsolutePath());
					parseXMLConfigFile(importedFile, configMap, importedLevel + 1);
				}
			}
		}

		@Override
		public void endElement(String uri,
			String localName,
			String qName) throws SAXException {
			if (!"configuration".equals(localName)) {
				LOGGER.trace("Put key '{}' with value '{}' to config table",
					localName, value.toString().trim());
				configMap.put(localName, value.toString().trim());
			}
			value.delete(0, value.length());
		}

		@Override
		public void characters(char[] ch, int start, int length)
			throws SAXException {
			value.append(ch, start, length);
		}
	}

}
