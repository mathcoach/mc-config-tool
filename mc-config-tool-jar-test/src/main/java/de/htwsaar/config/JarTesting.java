package de.htwsaar.config;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author hbui
 */
public class JarTesting {

    public static void main(String[] args) {                
        EnvConfiguration conf = new ClasspathBasedConfig("no-existing-config.properties", "jar-testing-config.properties");
        Set<String> config = conf.getAllConfigKeys();
        Map<String,String> expected = Map.of(
            "de.htwsaar.config.string", "test",
            "de.htwsaar.config.number", "123.456",
            "some.config.param", "some-value",
            "de.htwsaar.config.path", "/tmp/some/path.txt"
        );
        config.forEach(key -> 
            System.out.println("    " + key + " -> >>" + conf.getConfigValue(key) + "<< expected: >>" + expected.get(key) + "<<")//NOSONAR System.out in main is OK
        );        
    }
}
