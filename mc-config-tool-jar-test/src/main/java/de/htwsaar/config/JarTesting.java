package de.htwsaar.config;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author hbui
 */
public class JarTesting {

    public static void main(String[] args) throws URISyntaxException {
        final String primaryConfigFilename = "jar-testing-config.properties";
        final DummyKlass obj = new DummyKlass();
        final URI jarUlr =  obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        System.out.print("Provided jar-url: `");
        System.out.print(jarUlr);
        System.out.println("`");
        System.out.print("Provided config file: `");
        System.out.print(primaryConfigFilename);
        System.out.println("`");
    }
}
