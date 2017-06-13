package de.htwsaar.config;

import static de.htwsaar.config.EnvConfiguration.MAX_IMPORT_LEVEL;
import static de.htwsaar.config.EnvConfiguration.resolveSystemProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
class XMLConfigParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfiguration.class);
	
	Map<String,String> parseConfigFile(File configFile){
		Map<String,String> configTable = new HashMap<>();
		parseXMLConfigFile(configFile, configTable, 0);
		return configTable;
	}
	
	private void parseXMLConfigFile(
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
				Path p = configFile.toPath().toAbsolutePath().normalize();
				fileName = p.toString();
			}
			LOGGER.error("Cannot parse config file: {} ({})",
				fileName, ex.getMessage());
			LOGGER.trace("{}", ex);
			throw new LSConfigException("Error by parsing file " + fileName, ex);
		}catch(NullPointerException ex){
			throw new LSConfigException("Connfig file is null", ex);
		}
	}
	
	private class ConfigParser extends DefaultHandler {

		private final Map<String, String> configMap;
		private final StringBuilder value;
		private final int importedLevel;

		public ConfigParser(Map<String, String> configMap, int importedLevel) {
//			if (configMap == null) {
//				throw new IllegalArgumentException("Argument configMap must not be null");
//			}
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
					LOGGER.info("Parse import file {}", importedFile.getAbsolutePath());
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
