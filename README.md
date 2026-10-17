# Fit & Ziele – Fitness, Ernährung und Fortschritt

Eine deutschsprachige Android-App für Ernährung, Training, Schlaf und persönliche Ziele. Die App verbindet das FitApp-Trainingstagebuch mit dem Ziele-Kalender.

## Funktionen

- Ernährungstagebuch mit Kalorien, Eiweiß, Kohlenhydraten, Zucker, Fett und Ballaststoffen
- Lebensmittelsuche und lokaler Katalog mit 266 Lebensmitteln
- Barcode-Scanner mit optionalem Abgleich über Open Food Facts
- Eigene Lebensmittel und zuletzt verwendete Lebensmittel
- Ziele-Kalender, Trainingstagebuch, Schlafprotokoll und Fortschrittsübersicht
- Lokale Datenspeicherung mit Room; keine Cloud für persönliche Einträge

Die zusätzlichen Nährwertdatensätze stammen aus dem Bundeslebensmittelschlüssel (BLS) 4.0 des Max Rubner-Instituts und stehen unter CC BY 4.0. Details stehen in app/src/main/assets/FOOD_DATA_ATTRIBUTION.txt.

## APK aus GitHub Actions laden

Der Workflow baut bei Pushes auf main, Pull Requests gegen main und auf manuellen Start eine Debug-APK. Nach erfolgreichem Lauf:

1. GitHub-Reiter **Actions** öffnen und den Build-Lauf auswählen.
2. Unter **Artifacts** die Datei **fitness-debug-apk** herunterladen.
3. Das ZIP entpacken und app-debug.apk auf ein Android-Gerät kopieren.

Der Workflow installiert Gradle 8.7 selbst; ein Gradle-Wrapper-JAR wird dafür nicht benötigt.

## Lokaler Build

Android Studio, JDK 17 und Android SDK 34 werden benötigt. Mit lokal installiertem Gradle 8.7 lässt sich die Debug-APK mit gradle assembleDebug bauen. Die Datei liegt anschließend unter app/build/outputs/apk/debug/app-debug.apk.

Der Barcode-Produktabgleich benötigt eine Internetverbindung. Scannen und manuelles Eintragen bleiben voneinander unabhängig.
