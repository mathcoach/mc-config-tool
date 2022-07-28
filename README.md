# Anleitung

Version: 3.0.0

## Problem

Die Bibliothek `lib-a` braucht zur Laufzeit die Konfigurationsparameter (abk.:KP)
`db-user` und `db-password`. Diese KP wird als Parameter von *Konstruktor*
bzw. von *Setter* oder von *Builder* weiter gegeben, auf jedenfalls sind sie
als Argumenten einer Methode und nicht fest codiert. Der Wert des KP ist
an unterschiedlichen Laufzeitsumgebungen unterschiedlich.

* Zum Testen müssen die Test-Klassen die Werten der KP an produktiven Code
weiter leiten, es heißt, die oben genannten Methoden mit richtigen Werten
aufzurufen.

* Die Java Web-Anwendung `Web-x` und die Desktop Anwendung `App-z`
braucht die Bibliothek `lib-a`.
Also die Web-Anwendung `web-x` muss irgendwie die KP auch an Code in `lib-a`
weiterleiten.

Da verschiedenen Entwickler verschiedenen Werten der KP haben, und die
Web-Anwendung muss auf unterschiedlichen Maschinen deployable sein, darf man
die KP nicht in einem festen Datei, die in Source Code Management System
(SCM) angelegt wird. Also man braucht:

* Individuellen KP für Test
* Individuellen KP für Produktives

## Abstrakte Konzept für Maven Projekten

Die Bibliothek `lib-a` wird als einem Maven-Projekt organisiert. Die
Web-Anwendung `Web-x` und die Desktop-Anwendung `App-z` werden jeweils in einem
Maven-Projekt organisiert.

Da Maven die Source Datei strikt in zweit Teilen trennt: Produktives Code
(`src/main`) und Test Code (`src/test`), kann man die Konfiguration-Dateien in
Classpath (abk.: CP) platzieren, eine für Produktives Code und eine für Test
Code. Die Konfigurationsdateien werden in CP ausgelesen und geparsert.

Maven inkludiert Dateien und Ordner in `src/main/resource` bzw.
`src/test/resources` in jar-Datei, bzw in CP in der Test-Phase eingebunden.

Da ein Bibliothek in meisten Fälle keine `main()`-Method hat, welche die
Java-Code ausführt, braucht man für eine Bibliothek keine Konfigurationsdatei in
(`src/main/resources`) zu platzieren, sondern nur eine Konfigurationsdatei in
(`src/test/resources`). Die Konfiguration-Datei in `src/test/resouces` ist nur
in Test-Phase der Bibliothek in CP sichtbar.

In der Anwendung Projekten platziert man für die Laufzeit der Anwendung eine
Konfigurationsdatei in `src/main/resources` und für die Test-Phase eine
Konfiguration `src/test/resouces`. Somit hat man immer nur eine eindeutige
Konfigurationsdatei wenn die Code ausgeführt wird. Beispiel:

```text
lib-a
 |--src
    |--main
    |--test
       |--resources
            |--config-alternative-1.properties (Nicht sichtbar in Web-x)
            |--config-alternative-2.properties (Nicht sichtbar in Web-x)

Web-x (gilt auch für App-z)
 |--src
    |--main
    |   |--resources
    |       |--config.xml   (dient lediglig um Konfigurationsdatei in locale Umgebung zu verwerten)
    |--test
        |--resources
            |--config-alternative-1.properties (Nur in Test-Phase sichtbar)
            |--config-alternative-2.properties (Nur in Test-Phase sichtbar)
```

(Über die Format der Konfigurationsdatei (XML, Property, JSON) diskutieren wir
irgendwann.)

Mit diesem Konzept kann man die Konfiguration für Test und Laufzeit trennen.
Nun haben wir die Konfigurationsdateien für die Besonderheit jeweiliger
Umgebung, sprich Rechner von Entwickler, Server, ect.

Wir können die Konfigurationsdateien in CP so erweitern, dass sie
Konfigurationsdateien in Runtime-Umgebung inkludieren, die Parameter in der inkludierte
Konfigurationsdateien verwenden.

## Konkrete Implementierung

### Maven Artifact

Für die Nutzung von API:

```xml
<dependency>
    <groupId>io.github.mathcoach</groupId>
    <artifactId>mc-config-tool</artifactId>
    <version>${mc.config.version}</version>
</dependency>
```

Für die automatische Sammlung von Konfigurationsparameters:

```xml
<dependency>
    <groupId>io.github.mathcoach</groupId>
    <artifactId>mc-config-tool-anotation-processor</artifactId>
    <version>${mc.config.version}</version>
    <scope>compile</scope>
</dependency>
```

### Die Konfigurationsdatei

Die aktuelle Implementierung verwendet Properties-Format.
Die Konfigurationsdatei sieht etwa so aus:

```properties
# Laufzeit-Umgebung abhängig Parameter kann in einem Datei außerhalb der Source Code
# mit Hilfe von import konfiguriert werden:
#import=${HOME}/[application-name]/configuration.properties
```

Ein konkretes Beispiel:

```properties
# Filename: ${project.base}/src/main/resources/configuration.properties

import=${HOME}/online-glossary/configuration.properties

# NOTE:
# There is no sensible data in this file!
```

```properties
# Filename: $HOME/online-glossary/configuration.properties

glossary.database.url=jdbc:postgresql:glossary
glossary.database.username=sysad
glossary.database.password=topsecret

# NOTE:
# Sensible data are stored in a local machine-specified file, outside of
# project source base.
```



Mit der obigen Konfigurationsdatei kann man z.B. den Konfigurationsparameter
`glossary.database.username` als `sysad` und `glossary.database.password` als `topsecret`
erwarten. Man bekommt diesen Wert durch etwa

```java
// a static Factory class can be used to get an instance of EnvConfiguration at a
// position in application
EnvConfiguration env = new ClasspathBasedConfig("test-configuration.properties", "configuration.properties") ; //Obtain the Instance here

// ... some where in code
env.getConfigValue("glossary.database.username"); // → sysad
```


### Die Application Programming Interface

#### Anwendung in Bibliothek-Artig Projekt

Bibliothek-artige Projekten sind Projektion, dessen Code ein oder
mehrere Funktionalität für andere Projekten zur Verfügung stellt.

Eine Instanz der Interface `EnvConfiguration` repräsentiert die ganze
Konfiguration in einer Runtime-Umgebung. Somit kann man die Format der
Konfigurationsdatei abstrahieren. Für eine Änderung der Format von
Konfigurationsdatei muss man eine andere Implementierung der Interface
`EnvConfiguration` schreiben.

Die aktuelle Implementierung der Interface `EnvConfiguration` ist die Klasse
`ClasspathBasedConfig`. In einem Bibliothek Projekt soll man in produktive Code
nicht gegen diese Klasse programmieren, sondern nur gegen die Interface.
In Test-Code (etwa `src/test/java`) schreibt ein Konfiguration Manager,
die ein Instanz dieser Klasse erstellt, und es wird nur für Unit-Test bzw.
Integration-Test verwendet. Die Ordner-Struktur sieht so aus:

```text
lib-a
 |--src
    |--main
    |   +--java
    |       +--fqn
    |           +--ConfigUser.java
    +--test
        |--java
        |   +--fqn
        |       |--TestUnit_1.java
        |       +--ConfigurationManager.java
        +--resources
             |--config-alternative-1.xml (Nicht sichtbar in Web-x)
             +--config-alternative-2.xml (Nicht sichtbar in Web-x)
```

Die Beispiel-Code der Klasse `ConfigurationManager` könnte so aussehen:

```java
public class ConfigurationManager {
    public static EnvConfiguration getConfigAlternative1() {
        return new ClasspathBasedConfig("config-alternative-1.xml");
    }

    public static EnvConfiguration getConfigAlternative2() {
        return new ClasspathBasedConfig("config-alternative-2.xml");
    }
}
```

Die Klasse `ConfigUser` kann eine Method `setConfig(EnvConfiguration env)` haben,
die die Konfigurationsparameter auswerten.

In einer TestUnit-Klasse verwendet man die Klasse `ConfigurationManager` um die
Klasse `ConfigUser` zu konfigurieren, etwa

```java
@Test
public doTestWithConfigAlternative_1(){
    ConfigUser cu = new ConfigUser();
    cu.setConfig( ConfigurationManager.getConfigAlternative1() );
    //Do some test and assert here
}

@Test
public doTestWithConfigAlternative_2(){
    ConfigUser cu = new ConfigUser();
    cu.setConfig( ConfigurationManager.getConfigAlternative2() );
    //Do some test and assert here
}
```

Braucht man zusätzlich Konfigurationsparameter, die von Runtime-Umgebung abhängig sind,
kann man wie oben schon erwähnt, ein `import`-Property hinzufügen. Sinnvolle
Runtime-Umgebung-Konfigurationsparameter sind etwa Zugang-Daten für Test Datenbank
(aus Sicherheitsgrund), Timeout für *Longlife*-Thread (hängt von Kapazität der
Rechner ab (CPU/RAM/Network)).


#### Anwendung in Application-Artig Projekt

Application-Artig Projekten sind Projekten, die von (End)-Benutzer benutzt werden.
Sie sind z.B. eine Desktop Anwendung oder eine Web-Anwendung.

* Im Test-Phase (`src/test/resources`) von Application-artige Projekt kann man die Konzept wie ein
  Bibliothek-artige Projekt verwenden.

* In der Betrieb-Phase (`src/main/resources`) erstellt man eine Klasse, die eine Instanz
  der Interface `EnvConfiguration` erstellt etwa `WebConfigManager` oder
  `DesktopConfigManager`, und verwendet nur diese Klasse um andere Klasse in
  Bibliothek (etwa `ConfigUser`) oder in eignen Main-Bereich zu konfigurieren.
  Da die Klassen in Bibliothek die Interface `EnvConfiguration` verwendet, kann
  man zu jeder Zeit die Format der Konfigurationsdatei ändern, ohne die
  Bibliothek neu zu schreiben. Die einzige neu zuschreibende Klasse ist die
  Klasse `WebConfigManager` bzw. `DesktopConfigManager`.


#### Automatisch Sammlung von KP und Erstellung von Konfigurationsdatei

Es ist umständlich, ein Software mit umfangreiche KP Deployment zu machen. Die Überblick
über KP ist schnell verloren. Von daher ist es gut, jede Module verwaltet seine KP
selbst und eine zentral Stelle sammelt die KP, wenn die Software deploy wird.

Die Annotation `@NeedConfig` ermöglicht eine Klasse, ihre erforderliche KP zu
*deklarieren*.  Die Nutzung sieht etwa aus:

In POM-Datei:

```xml
<dependency>
    <groupId>io.github.mathcoach</groupId>
    <artifactId>mc-config-tool-anotation-processor</artifactId>
    <version>${mc.config.version}</version>
    <scope>compile</scope>
</dependency>
```

In Java-Datei, welche ein Konfiguration-Parameter braucht.

```java
@NeedConfig(
    name= "Name der Konfiguration-parameter 1",
    description= "Die Beschreibung der KP",
    sugguestValues= {
        "Value 1",
        "Value 2"
    }
)
public class AJavaClass{

}
```

Seit Java 8 kann man Multiple Annotation benutzen, deshalb kann man für eine
Klasse auch mehrfach die Annotation `NeedConfig` anwenden. Von Java 7 abwärts kann man
etwa so schreiben:

```java
@NeedConfigs({
    @NeedConfig(
        name= "Name der Konfiguration-parameter 1",
        description= "Die Beschreibung der KP",
        sugguestValues= {
            "Value 1",
            "Value 2"
        }
    ),
    @NeedConfig(
        name= "Name der Konfiguration-parameter 2",
        description= "Die Beschreibung der KP",
        sugguestValues= {
            "Value 1",
            "Value 2"
        }
    )
})
public class AJavaClass{

}
```

Die Annotation hat keine Einfluss auf der Laufzeit einer Klasse, sie dient nur
dazu die Konfigurationsparameter einer Klasse zu markieren, damit sie während
der Kompilierungszeit gesammelt werden kann. Wenn man die Klasse der Annotation `@NeedConfig`
bereits markiert, kann man die KP zur Laufzeit sammeln:

```java
Map<String, ConfigEntries.Entry> usedConfigParam = ConfigCollector.collectConfigAsMap();
```

Diese Method ist zum Beispiel praktisch, wenn man die gesamte Anwendung nach der Installation,
Deployment konfigurieren will. Man kann anhand der Map alle nötigen KP in der Anwendung
ablesen und in einer Konfigurationsdatei schreiben.



