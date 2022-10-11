package de.htwsaar.config;

/**
 *
 * @author hbui
 */
public class ConfigFileNotFoundException extends LSConfigException {

    public ConfigFileNotFoundException(String primaryConfigFileName, String secondaryConfigFileName) {
        super("Primary config file '" + primaryConfigFileName + "' and secondary config file '" + secondaryConfigFileName + "' not found in classpath.");
    }

}
