package de.htwsaar.config;

import java.io.File;
import java.util.Objects;

/**
 *
 * @author hbui
 */
public final class ConfigParserFactory {
	
	private ConfigParserFactory(){
		//prevent to create an instance of this class
	}
	
	public static final ConfigParser getParserForFile(File configFile){
		Objects.requireNonNull(configFile, "Config file must not be null");
		String fileName = configFile.getName();
		if(fileName.endsWith("xml")){
			/*return new XMLConfigParser();*/
            throw new RuntimeException("xml is not supported any more");
		}else if(fileName.endsWith("properties")){
			return new PropertiesConfigParser();
		}else{
			throw new LSConfigException("Parser for file " + fileName + " not found");
		}
	}
			
}
