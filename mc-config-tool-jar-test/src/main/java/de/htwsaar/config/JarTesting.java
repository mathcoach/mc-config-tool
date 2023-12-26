package de.htwsaar.config;

import java.util.Set;

/**
 *
 * @author hbui
 */
public class JarTesting {

    public static void main(String[] args) {                
        EnvConfiguration conf = new ClasspathBasedConfig("no-existing-config.properties", "jar-testing-config.properties");
        Set<String> config = conf.getAllConfigKeys();
        config.forEach(key -> 
            System.out.println("    " + key + " -> " + conf.getConfigValue(key))//NOSONAR System.out in main is OK
        );        
    }
}
