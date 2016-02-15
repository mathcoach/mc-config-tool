# Anleitung

## Problem

Die Bibliothek `lib-a` braucht zur Laufzeit die Konfigurationsparameter (abk.:KP)
`db-user` und `db-password`. Diese KP wird als Parameter von *Konstruktor* 
bzw. von *Setter* oder von *Builder* weiter gegeben, auf jedenfalls sind sie
als Argumenten einer Methode und nicht fest codiert. Der Wert des KP ist
an unterschiedlichen Stelle unterschiedlich.

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

Die Bibliothek `lib-a` wird in einem Maven-Projekt eingegliedert. Die
Web-Anwendung `Web-x` und die Desktop-Anwendung `App-z` werden jeweils in einem
Maven-Projekt eingegliedert.

Da Maven trennt die Source Datei strikt in zweit Teilen: Produktives Code
(`src/main`) und Test Code (`src/test`), kann man eine Konfiguration-Datei in
Classpath (abk.: CP) platzieren, eine für Produktives Code und eine für Test
Code. Die Konfigurationsdatei wird in CP ausgelesen und geparsert.

Maven inkludiert Dateien und Ordner in `src/main/resource` bzw.
`src/test/resources` in jar-Datei, bzw in CP in der Test-Phase eingebunden.

Da ein Bibliothek in meisten Fälle keine `main()`-Method hat, welche die
Java-Code ausführt, braucht man für eine Bibliothek eine Konfigurationsdatei in
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
	        |--config-alternative-1.xml (Nicht sichtbar in Web-x)
			|--config-alternative-2.xml (Nicht sichtbar in Web-x) 

Web-x (gilt auch für App-z)
 |--src
    |--main
	|   |--resources
	|       |--config.xml
	|--test
	    |--resources
		    |--config-alternative-1.xml (Nur in Test-Phase sichtbar)
			|--config-alternative-2.xml (Nur in Test-Phase sichtbar)
```

(Über die Format der Konfigurationsdatei (XML, Property, JSON) diskutieren wir
später.)

Mit diesem Konzept kann man die Konfiguration für Test und Laufzeit trennen.
Nun müssen wir die Konfiguration für die Besonderheit jeweiliger
Runtime-Umgebung, sprich Rechner von Entwickler, Server, ect.

Wir erweitern die Konfigurationsdateien in CP so, dass sie
Konfigurationsdateien in Runtime-Umgebung inkludieren und statt die KP in
CP-Konfigurationsdateien die KP in Runtime-Umgebung nutzen.

## Konkrete Implementierung

### Maven Artifact

Für die Nutzung von API:

```xml
<dependency>
	<groupId>de.htwsaar</groupId>
	<artifactId>mc-config-tool</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

Für die automatische Sammlung von Konfigurationsparameters:

```xml
<dependency>
	<groupId>de.htwsaarland</groupId>
	<artifactId>mc-config-tool-anotation-processor</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>compile</scope>
</dependency>
```

### Die Konfigurationsdatei

Die aktuelle Implementierung nutzt aus historische Grund die XML-Format um die
KP zu speichern.  Die Konfigurationsdatei sieht etwa so aus:

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration import="[path in file system]">
	<config-param-1>wert-1</config-param-1>
	<config-param-2>wert-2</config-param-2>
</configuration>	
```

Der Root-Element der Konfigurationsdatei ist `configuration`. Es hat ein
optionales Attribute `import`, das nur abgelesen wird, wenn die
Konfigurationsdatei in CP ist.

Jede KP ist durch ein Element (wir nennen es Konfiguration-Element)
repräsentiert, dessen Kind-Node ist entweder ein Text-Node oder CDATA-Node, und
repräsentiert dessen Konfigurationswerten.  Die führenden Leerzeichen und die
endende Leerzeichen werden einfach ignoriert. Regel:

* Existiert kein Konfiguration-Element, wird dessen Wert als `null`
  repräsentiert. Ist der Konfigurationswert ein Leerstring, ist es auch als
  Leerstring repräsentiert.

* Kommt ein KP sowohl ind CP-Konfigurationsdatei als auch in
  `import`-Konfigurationsdateien vor, wird der Wert in CP-Konfigurationsdatei
  verwendet.

* Kommt das Attribute `import` in einer `import`-Konfigurationsdatei vor, wird
  ein Exception ausgeworfen. In `import` kann man folgenden Variable benutzen:

  * `${HOME}` bzw. `$HOME`, oder `${user.home}` das Home-Verzeichnis der User,
	welche das Java-Prozess startet.  Es wird in `user.home`-Sytem Property
	übersetzt.
  * `${PWD}` bzw. `$PWD`, oder `${user.dir}` das aktuelle Verzeichnis des
	Java-Prozesses. Man soll diese Variable vermeiden wenn es möglich ist.

Konkrete Beispiel:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- File config-test.xml in CP -->
<configuration import="${HOME}/mathcoach/laplus-config-test.xml">
    <class-path>target/test-classes</class-path>
    <author-root>src/test/resources/virtual-file-system</author-root>
	<application-path>/tmp/mathcoach-app</application-path>
</configuration>
```

und 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- File ${HOME}/mathcoach/laplus-config-test.xml in File System -->
<configuration>
    <db-password>topsecret</db-password>
</configuration>
```

Mit der obigen Konfigurationsdatei kann man z.B. den Konfigurationsparameter
`author-root` als `target/test-classes` und `db-password` als `topsecret`
erwarten. Man bekommt diesen Wert durch etwa

```java
EnvConfiguration env = ... ; //Obtain the Instance here, see section below
env.getConfigValue("author-root");
```
bekommen. 

> Bemerkung: Die Interpretation von `target/test-classes` hängt natürlich von
> der Anwendung ab,
  hat nichts mit Config zu tun.

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
kann man wie oben schon erwähnt, ein `import` Attribute in `configuration`-Element
hinzufügen. Sinnvolle Runtime-Umgebung-Konfigurationsparameter sind etwa Zugang-Daten
für Test Datenbank (aus Sicherheitsgrund), Timeout für *Longlife*-Thread (hängt von
Kapazität der Rechner ab (CPU/RAM/Network)).


#### Anwendung in Application-Artig Projekt

Application-Artig Projekten sind Projekten, die von (End)-Benutzer benutzt werden.
Sie sind z.B. eine Desktop Anwendung oder eine Web-Anwendung.

* Im Test-Bereich von Application-artige Projekt kann man die Konzept wie ein
  Bibliothek-artige Projekt verwenden. 

* In der Main-Bereich (`src/main`) erstellt man eine Klasse, die eine Instanz
  der Interface `EnvConfiguration` erstellt etwa `WebConfigManager` oder
  `DesktopConfigManager`, und verwendet nur diese Klasse um andere Klasse in
  Bibliothek (etwa `ConfigUser`) oder in eignen Main-Bereich zu konfigurieren.
  Da die Klassen in Bibliothek die Interface `EnvConfiguration` verwendet, kann
  man zu jeder Zeit die Format der Konfigurationsdatei ändern, ohne die
  Bibliothek neu zu schreiben. Die einzige neu zuschreibende Klasse ist die
  Klasse `WebConfigManager` bzw. `DesktopConfigManager`.




