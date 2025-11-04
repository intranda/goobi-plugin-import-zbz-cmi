---
title: Datenimport mit CMI-Katalogabfrage für die Zentralbibliothek Zürich
identifier: intranda_import_zbz_cmi
description: Dieses Import Plugin für Goobi workflow erlaubt das Einspielen von Daten mit anschließender Katalogabfrage aus CMI, wie es für die Zentralbibliothek Zürich benötigt wird.
published: true
keywords:
    - Goobi workflow
    - Plugin
    - Import Plugin
---

## Einführung
Dieses Import-Plugin erlaubt das Einspielen von Daten mit CMI-Katalogabfrage. Dabei werden in der Nutzeroberfläche Daten eingefügt, die zuvor aus einer Excel-Datei kopiert wurden.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/import/plugin-import-zbz-cmi-base.jar
/opt/digiverso/goobi/config/plugin_intranda_import_zbz_cmi.xml
```

Nach der Installation des Plugins, kann dieses aus der Übersicht der Produktionsvorlagen durch Nutzung des zweiten blauen Buttons neben der gewählten Produktionsvorlage betreten werden.

![Produktionsvorlage mit zusätzlichem blauen Button für den Massenimport](screen1_de.png)

Wenn das Plugin betreten wurde, steht eine Nutzeroberfläche zur Verfügung, in der die einzuspielenden Daten ausgewählt bzw. hochgeladen werden können.

![Nutzeroberfläche des Import-Plugins](screen2_de.png)


## Überblick und Funktionsweise
Nach der Auswahl des richtigen Plugins können in der Nutzeroberfläche die Daten die entweder als CSV-Daten TAB-getrennt vorliegen oder die aus einer Excel-Datei kopiert werden in das Feld `Datensätze` eingefügt werden. Die Daten haben dabei den folgenden Aufbau:

Spalte    | Metadatum       | Erläuterung
----------|-----------------|-------------------------
`1`       | `CMI-ID`        | Wenn diese einen Unterstrich enthält, wird ein Mehrbändiges Werk angelegt, andernfalls eine Monographie. Hierbei handelt es sich um eine Pflichtangabe.

Unmittelbar nach dem Einfügen der Daten und dem Klick auf `Speichern` startet das Anlegen der Vorgänge, ohne dass dabei ein Katalog abgefragt wird.


## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_import_zbz_cmi.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

Die folgende Tabelle enthält eine Zusammenstellung der Parameter und ihrer Beschreibungen:

Parameter               | Erläuterung
------------------------|------------------------------------
`template`              | Hiermit kann festgelegt werden für welche Produktionsvorlabe der jeweilige `config`-Block gelten soll. 
`runAsGoobiScript`      | Mit diesem Parameter kann festgelegt werden, ob der Import als GoobiScript im Hintergrund stattfinden soll.
`catalogue`             | Hier wird definiert, welcher Katalog für die Abfrage verwendet werden soll. Diese muss innerhalb der Konfigurationsdatei `goobi_opac.xml` definiert worden sein.
`searchField`           | Mit diesem Parameter wird festgelegt, in welchem Feld des Katalogs die Suche nach dem Identifier erfolgen soll.