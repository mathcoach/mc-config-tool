package de.htwsaar.config;

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
public class XMLConfigParser implements ConfigParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfiguration.class);
	
	@Override
	public Map<String,String> parseConfigFile(File configFile){
		Map<String,String> configTable = new HashMap<>();
		parseXMLConfigFile(configFile, configTable);
		return configTable;
	}
	
	
	private void parseXMLConfigFile(
		final File configFile,
		final Map<String, String> configTable) {

		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new ConfigParser(configTable/*, importedLevel*/));
			xmlReader.parse(configFile.toURI().getPath());
			if (LOGGER.isTraceEnabled()) {
				Set<Map.Entry<String, String>> entrySet = configTable.entrySet();
				entrySet.forEach( config -> LOGGER.trace("{} -> {}", config.getKey(), config.getValue()) );
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
				String importAtt = attributes.getValue("import");
				if(importAtt!=null){
					this.configMap.put("import", importAtt);
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
