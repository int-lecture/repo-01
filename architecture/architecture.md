# Komponenten

## User interface (UI)
### Eigenschaften
* stellt inhalte wie Nachrichten, Daten, etc. anschaulich dar
* kümmert sich um den Input durch Maus und Tastatur
### Skalierbarkeit
Komplett skalierbar, jeder Nutzer hat selbstverständlich sein eigenes UI.

## Load balancer
### Eigenschaften
* teilt Verbindungen gleichmäßig auf die Webserver auf
* hat eine Liste aller verfügbaren Webserver
### Skalierbarkeit
Nicht oder sehr schlecht skalierbar, da die Verteilung auf die Webserver konsistent sein muss.

## Webserver
### Eigenschaften
* stellt Inhalte bereit
* kommuniziert mit den anderen Servern
### Skalierbarkeit
Sehr gut skalierbar, da die Webserver selbst keine Daten halten.

## Server für statische Inhalte
### Eigenschaften
* stellt versendete Dateien zur Verfügung
* löscht Dateien, nachdem diese abgerufen wurden
### Skalierbarkeit
Sehr schlecht skalierbar, da die Daten sehr flüchtig sind.

## Login server
### Eigenschaften
* kümmert sich um die Authentifizierung der Benutzer
* hat eine Lister aller Benutzer und deren Passwörter (verschlüsselt)
### Skalierbarkeit
Nicht oder sehr schlecht skalierbar, da jeder Nutzer nur einmal eingeloggt sein darf und mehrere Login server diese Integrität leicht verletzen könnten.

## Account server
### Eigenschaften
* stellt nutzerspezifische Daten bereit
* hat eine Datenbank für Profile
* hat eine Datenbank für Kontakte und blockierte Benutzer
### Skalierbarkeit
Mäßig skalierbar, da Änderungen an den Daten überall übernommen werden müssen. Die Daten sind allerdings relativ statisch (nicht flüchtig).

## Nachrichten server
### Eigenschaften
* speichert versendete Nachrichten und stellt diese zum Abruf bereit
* hat eine Datenbank für Chatverläufe
### Skalierbarkeit
Sehr schlecht skalierbar, da die Daten sich ständig ändern.

## Verschlüsselungs-server
### Eigenschaften
* verschlüsselt Nachrichten
### Skalierbarkeit
Sehr gut skalierbar, da keine Daten gehalten werden.
