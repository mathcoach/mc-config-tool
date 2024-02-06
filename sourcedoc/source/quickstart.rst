*************************
Quickstart in three steps
*************************

In this section we walk through a common situation which happens when one writes a Java Application. We assume that
Maven is used for building the application and for dependency management.


Your app needs information about the database URL, the database user and the database password.
These information should not be exposed on any source code repository. So you want to put these information in a local directory
outside of the source code, say in

* ``${HOME}/myapp/configuration.properties`` for running the app locally, and
* ``${HOME}/myapp/configuration-test.properties`` for running the unit test of the app, also locallly.

``${HOME}`` denotes the home-directory of the user, who starts the Java-process. It is resolved in Linux
mostly to ``/home/userid`` and in Windows mostly to ``C:\\Users\\userid``.

If your app is delivered to users, and runs on other machines, so users can create a configuration file
``${HOME}/myapp/configuration.properties`` on their computer.





1. Update Maven pom.xml
=======================

Extend the Maven pom.xml with this dependency:

.. code-block:: xml

   <properties>
       <mc.config.verion>4.2.4</mc.config.version>
   </properties>

   <dependencies>
      <!-- others dependencies -->
      <dependency>
          <groupId>io.github.mathcoach</groupId>
          <artifactId>mc-config-tool</artifactId>
          <version>${mc.config.version}</version>
      </dependency>
   </dependencies>


2. Manage your configuration
============================

Create one class to *provide* the single instance of ``EnvConfiguration`` [#f1]_:


.. code-block:: java

   import de.htwsaar.config.ClasspathBasedConfig;
   import de.htwsaar.config.EnvConfiguration;

   public final class AppConfig {

       public static final String TEST_CONFIG = "configuration-test.properties";
       public static final String PRODUCTIVE_CONFIG = "configuration.properties";

       private static EvnConfiguration config;

       public static EnvConfiguration getConfig() {
           if (config == null) {
               synchronized (AppConfig.class) {
                   config = new ClasspathBasedConfiguration(
                       TEST_CONFIG,        // configuration for test environment
                       PRODUCTIVE_CONFIG   // configuration for productive environment
                  );
               }
           }
           return config;
      }

      private AppConfig() {
         //prevent to create an instance of this class.
      }
   }


If a class needs to *consume* a configuration parameter, it can use one of these patterns:

**Consume an instance of EnvConfiguration**

.. code-block:: java

   class DbConnector {

       public static final String DB_URL_KEY = "db.url";
       public static final String DB_USER_KEY = "db.username";
       public static final String DB_PASS_KEY = "db.password";

       DataSource getDataSource(EnvConfiguration config) {
           String dbUrl = config.getConfigValue(DB_URL_KEY);
           String dbUser = config.getConfigValue(DB_USER_KEY);
           String dbPass = config.getConfigValue(DB_PASS_KEY);
           // create an instance of DataSource using variables dbUrl, dbUser, dbPass
           // ....
       }
   }


This pattern can be used when a method needs many information. In this case, it is not comfortable to put them to
the argument list of the method. This pattern is testable, using the class ``de.htwsaar.config.DynamicConfig`` [#f2]_.


**Parametrize Method**

.. code-block:: java

   class DbConnector {

       DataSource getDataSource(String dbUrl, String dbUser, String dbPassword) {
           // create an instance of DataSource using the arguments
           // ...
       }
   }

   public class MyApp {

       public static final String DB_URL_KEY = "db.url";
       public static final String DB_USER_KEY = "db.username";
       public static final String DB_PASS_KEY = "db.password";


       public static void main(String[] argv) {

           // somewhere in main(String[] argv)
           EvnConfiguration config = AppConfig.getConfig();
           String dbUrl = config.getConfigValue(DB_URL_KEY);
           String dbUser = config.getConfigValue(DB_USER_KEY);
           String dbPass = config.getConfigValue(DB_PASS_KEY);
           DbConnector connector = new DbConnector();
           DataSource = connector.getDataSource(dbUrl, dbUser, dbPass);
       }
   }


The class ``DbConnector`` in this pattern is also testable, calling the method ``DbConnector.getDataSource`` is not
user-frendly. One must remember the order of 3 arguments. When  more than configuration values are needed,
the first pattern is the better choice in most cases.


**Directly call AppConfig.getConfig()**

.. code-block:: java

   // Anti-pattern!
   class DbConnector {

       public static final String DB_URL_KEY = "db.url";
       public static final String DB_USER_KEY = "db.username";
       public static final String DB_PASS_KEY = "db.password";


       DataSource getDataSource() {
           EvnConfiguration config = AppConfig.getConfig();
           String dbUrl = config.getConfigValue(DB_URL_KEY);
           String dbUser = config.getConfigValue(DB_USER_KEY);
           String dbPass = config.getConfigValue(DB_PASS_KEY);
           // create an instance of DataSource using variables dbUrl, dbUser, dbPass
           // ...
       }
   }


The class ``DbConnector`` in this pattern is only limited testable. It depends on the configuration in the file
``${HOME}/myapp/configuration-test.properties`` during the running of unit-test respectively the file
``${HOME}/myapp/configuration.properties`` during the productive running. So some edge-case such as wrong configuration,
database does not exist, ect. cannot be tested in unittest. (At least without changing the configuration)

This pattern is only suitable in the initial code of an app, for example in the ``main(String[] argv)`` or
initial code for Web-Application.


3. Configure your app
=====================

**You as lead developer :)**

Create a file called ``configuration-test.properties`` in the folder ``src/test/resources`` of your project:


.. code-block:: properties

   # File src/test/resources/configuration-test.properties
   import = ${HOME}/myapp/configuration-test.properties

This file imports the configuration file for unit test:

.. code-block:: properties

   # File ${HOME}/myapp/configuration-test.properties
   db.url = jdbc:postgresql:myapp-test
   db.username = myapp-user
   db.password = some-secret-password


Create a file called ``configuration.properties`` in the folder ``src/main/resources`` of your project:

.. code-block:: properties

   # File src/main/resources/configuration.properties
   import = ${HOME}/myapp/configuration.properties

This file imports the configuration file for the app as runtime on your developing computer.


.. code-block:: properties

   # File ${HOME}/myapp/configuration.properties
   db.url = jdbc:postgresql:myapp
   db.username = myapp
   db.password = other-secret-password

**Other developers**

Other developer in your teams have to create both files

* ``${HOME}/myapp/configuration-test.properties`` to run unit test on their computer, and
* ``${HOME}/myapp/configuration.properties`` to run the app it selft on their computer.


**Your Software user**

Your users, which just want to run your app, have to create only the file ``${HOME}/myapp/configuration.properties``
with appropriate configuration value of their computer.



Where to go next
----------------

* :doc:`parser-properties-file`
* :doc:`gen-config`


Indices and tables
------------------

* :ref:`genindex`
* `Javadoc <./apidocs/index.html>`_
* :ref:`search`


Classes in this section
-----------------------

.. [#f1] ``EnvConfiguration`` `<apidocs/de/htwsaar/config/EnvConfiguration.html>`_
.. [#f2] ``DynamicConfig`` `<apidocs/de/htwsaar/config/DynamicConfig.html>`_



