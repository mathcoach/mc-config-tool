/*
 * Copyright 2025 hbui.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.htwsaar.config;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author hbui
 */
public class JarJarTesting {

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
            System.out.println("    " + key + " -> >>" + conf.getConfigValue(key) 
                + "<< expected: >>" + expected.get(key) + "<<")//NOSONAR System.out in main is OK
        );        
    }
}
