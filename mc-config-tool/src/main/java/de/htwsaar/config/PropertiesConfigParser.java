package de.htwsaar.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author hbui
 */
public class PropertiesConfigParser implements ConfigParser {

	@Override
	public Map<String, String> parseConfigFile(File configFile) {
		try(InputStream s = new FileInputStream(configFile)){
			Map<String,String> configTable = new HashMap<>();
			Properties p = new Properties();
			p.load(s);
			p.forEach( (key, value) -> configTable.put(key.toString(), value.toString() ));
			return configTable;
		}catch(IOException ex){
			throw new LSConfigException(ex);
		}
	}
	
}
