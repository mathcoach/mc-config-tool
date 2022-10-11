package de.htwsaar.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author hbui
 */
public interface ConfigParser { //NOSONAR
      
    
    Map<String, String> parseConfigFile(InputStream configFile);

    default Map<String, String> parseConfigFile(File configFile){
        try (InputStream in = new FileInputStream(configFile)) {
            return parseConfigFile(in);
        }catch(IOException ex) {
            throw new LSConfigException(ex);//NOSONAR
        }catch (NullPointerException ex) {
            throw new LSConfigException("Config file must not be null", ex);
        }
    }
    
    default void reset() {
        //Nothing
    }
}
