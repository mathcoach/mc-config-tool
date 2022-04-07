/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package de.htwsaar.config.text;

import java.util.Map;


/**
 * Create instances of string lookups or access singleton string lookups implemented in this package.
 * <p>
 * The "classic" look up is {@link #mapStringLookup(Map)}. Also
 * {@link #systemPropertyStringLookup()} can be used.
 * </p>
 *
 */
final class StringLookupFactory {

    /**
     * Defines the singleton for this class.
     */
    public static final StringLookupFactory INSTANCE = new StringLookupFactory();



    /**
     * Defines the FunctionStringLookup singleton for looking up system properties.
     */
    static final FunctionStringLookup<String> INSTANCE_SYSTEM_PROPERTIES = FunctionStringLookup.on(System::getProperty);



    /**
     * Returns a new map-based lookup where the request for a lookup is answered with the value for that key.
     *
     * @param <V> the map value type.
     * @param map the map.
     * @return a new MapStringLookup.
     */
    <V> StringLookup mapStringLookup(final Map<String, V> map) {
        return FunctionStringLookup.on(map);
    }



    /**
     * Returns the SystemPropertyStringLookup singleton instance where the lookup key is a system property name.
     *
     * <p>
     * Using a {@link StringLookup} from the {@link StringLookupFactory}:
     * </p>
     *
     * <pre>
     * StringLookupFactory.INSTANCE.systemPropertyStringLookup().lookup("os.name");
     * </pre>
     * <p>
     * Using a {@link StringSubstitutor}:
     * </p>
     *
     * <pre>
     * StringSubstitutor.createInterpolator().replace("... ${sys:os.name} ..."));
     * </pre>
     * <p>
     * The above examples convert {@code "os.name"} to the operating system name.
     * </p>
     *
     * @return The SystemPropertyStringLookup singleton instance.
     */
    StringLookup systemPropertyStringLookup() {
        return StringLookupFactory.INSTANCE_SYSTEM_PROPERTIES;
    }

}
