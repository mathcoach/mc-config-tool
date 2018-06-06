package de.htwsaar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.htwsaar.config.EnvConfiguration.IMPORT_KEY;

/**
 *
 * @author hbui
 */
public class XMLConfigParser implements ConfigParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLConfigParser.class);

	@Override
	public Map<String, String> parseConfigFile(File configFile) {
		Map<String, String> configTable = new HashMap<>();
		parseXMLConfigFile(configFile, configTable);
		return configTable;
	}

	private void parseXMLConfigFile(
			final File configFile,
			final Map<String, String> configTable) {

		try (InputStream stream = new FileInputStream(configFile)) {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			xmlReader.setContentHandler(new ConfigParser(configTable));
			//xmlReader.parse(configFile.toURI().getPath());
			xmlReader.parse(new InputSource(stream));
			
			if (LOGGER.isTraceEnabled()) {
				Set<Map.Entry<String, String>> entrySet = configTable.entrySet();
				entrySet.forEach(config -> LOGGER.trace("{} -> {}", config.getKey(), config.getValue()));
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
		} catch (NullPointerException ex) {
			throw new LSConfigException("Connfig file is null", ex);
		}
	}

	private static class ConfigParser extends DefaultHandler {

		private final Map<String, String> configMap;
		private final StringBuilder value;

		public ConfigParser(Map<String, String> configMap) {
			this.configMap = configMap;
			value = new StringBuilder(100);
		}

		@Override
		public void startElement(String uri,
				String localName,
				String qName,
				Attributes attributes)
				throws SAXException {
			LOGGER.trace("Start element '{}'", localName);
			if ("configuration".equals(qName)) {
				String importAtt = attributes.getValue(IMPORT_KEY);
				if (importAtt != null) {
					this.configMap.put(IMPORT_KEY, importAtt);
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
