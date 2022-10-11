package de.htwsaar.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author hbui
 */
public final class PropertiesConfigParser implements ConfigParser {

    @Override
    public Map<String, String> parseConfigFile(InputStream configFile) {
        try ( InputStreamReader r = new InputStreamReader(configFile) ) {
            Map<String, String> configTable = new HashMap<>();
            Properties p = new Properties();
            p.load(r);
            p.forEach((key, value) -> configTable.put(key.toString(), value.toString().trim()));
            return configTable;
        } catch (IOException ex) {//NOSONAR
            throw new LSConfigException(ex);//NOSONAR
        } catch (NullPointerException ex) {
            throw new LSConfigException("Config file must not be null", ex);
        }
    }

}
