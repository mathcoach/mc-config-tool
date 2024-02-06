**********************************
How Configuration Files are Parsed
**********************************


The class ``ClasspathBasedConfiguration`` accepts a properties-file as configuration.
It also uses ``java.util.Properties.load(InputStream)`` to parsed the configuration file. So
a valid properties file is always a syntactical valid configuration file.


Whitespaces handling
====================

Leading and trailing whitespaces in values are removed. This behavior matches our requirement. It avoids ambiguous values
and lets users detect error in configuration file fast. In this example

.. code-block:: properties

    weird-config = \t

the value of ``weird-config`` is the empty string.


If you really want a configuration value as whitespaces, just write explicit what you need. For example, your app
must parse a CSV file, which either uses one space or tab or an other visible character (e.g. ``,`` or ``;``) for column
separator, you can write configuration like this:

.. code-block:: properties

    # if this is set to true, use space for column separator,
    # ignore other configuration for CSV column separator
    space_as_separator = false

    # if space_as_separator not set|false and this set to true , use tab for column separator,
    # ignore other configuration for CSV column separator
    tab_as_separator = false

    # if both space_as_separator and tab_as_separator are not set or are parsed to false,
    # use the value of this configuration for CSV column separator
    separator_char = ;

of course you must write code to evaluate the configuration. ``EnvConfiguration`` provides all configured parameters.
You must check the configured parameter in you desired order:


.. code-block:: java


    public static final String SPACE_SEPARATOR_CONFIG = "space_as_separator";
    public static final String TAB_SEPARATOR_CONFIG = "tab_as_separator";
    public static final String SEPARATOR_CHAR_CONFIG = "separator_char";

    public static final char DEFAULT_SEPARATOR = ';';

    char getCsvColumnSeparator(EnvConfiguration config) {

        BiFunction<String,String,Boolean> parseBooleanConfig = (param, value) ->  Boolean.parseBoolean(value);

        if( config.getConfigValue(SPACE_SEPARATOR_CONFIG, parseBooleanConfig ) ) {
            return ' ';
        }
        if( config.getConfigValue(TAB_SEPARATOR_CONFIG, parseBooleanConfig ) ) {
            return '\t';
        }
        return config.getConfigValue(SEPARATOR_CHAR_CONFIG, (param, value) -> {
            try {
                return value.charAt(0);
            } catch(StringIndexOutOfBound ex) {
                // in the case, that the configuration is an empty string
                return DEFAULT_SEPARATOR;
            }
        });
    }



Substitution
============

The two annotations ``${PWD}`` / ``$PWD`` and ``${HOME}`` / ``$HOME`` are resolved to Working directory of the
Java Process and the Home directory of the Java Process Owner respectively. So you shoud not use `PWD` and `HOME`
as your configuration parameter. The mechanic to resolve these annotations relies on Java mechanic to resolve
System environment ``user.home`` and ``user.dir``. So it should work on platforms, which Java support.
This library is howerver only tested with Linux.


A Parameter can make a reference to an other parameter in its value. The embeded parameter is also resolved to its value.
For example:

.. code-block:: properties

    myapp.base = $HOME/myapp
    myapp.data = ${myapp.base}/data
    myapp.conf = ${myapp.base}/conf

Of course you cannot make a circular reference, directly or indirectly. In this case an exception is thrown.