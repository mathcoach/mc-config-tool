package de.htwsaar.config;

import java.io.File;
import java.util.Map;

/**
 *
 * @author hbui
 */
public interface ConfigParser { //NOSONAR

    Map<String, String> parseConfigFile(File configFile);

    default void reset() {
        //Nothing
    }
}
