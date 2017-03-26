# Komponenten

## User interface (UI)
* stellt inhalte wie Nachrichten, Daten, etc. anschaulich dar
* kümmert sich um den Input durch Maus und Tastatur

## Load balancer
* teilt Verbindungen gleichmäßig auf die Webserver auf
* hat eine Liste aller verfügbaren Webserver

## Webserver
* stellt Inhalte bereit
* kommuniziert mit den anderen Servern

## Server für statische Inhalte
* stellt versendete Dateien zur Verfügung
* löscht Dateien, nachdem diese abgerufen wurden

## Login server
* kümmert sich um die Authentifizierung der Benutzer
* hat eine Lister aller Benutzer und deren Passwörter (verschlüsselt)

## Account server
* stellt nutzerspezifische Daten bereit
* hat eine Datenbank für Profile
* hat eine Datenbank für Kontakte und blockierte Benutzer

## Nachrichten server
* speichert versendete Nachrichten und stellt diese zum Abruf bereit
* hat eine Datenbank für Chatverläufe

## Verschlüsselungs-server
* verschlüsselt Nachrichten
