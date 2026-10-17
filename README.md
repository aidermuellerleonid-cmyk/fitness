# Fit & Ziele – Fitness, Ernährung & Fortschritt (Android)

Eine deutschsprachige Android-App zum Protokollieren von Ernährung, Training, Schlaf
und Fortschritt. Kotlin + Jetpack Compose + Room (lokale Datenhaltung, keine Cloud
erforderlich).

Der integrierte Tab **Ziele** enthält außerdem den Ziele-Kalender mit täglichem
Tracking für Schlaf, Kalorienziel, Lernen und Sport. Die fünf bestehenden
FitApp-Bereiche und der Barcode-Scanner bleiben in derselben App verfügbar.

## Funktionsüberblick

- **Einrichtungsassistent** beim ersten Start: Ziel, Körperdaten, Aktivitätslevel,
  Trainingserfahrung, Kalorien-/Nährstoffziele mit berechnetem Vorschlag. Jeder
  Schritt ist überspringbar, alles später im Profil änderbar.
- **Ernährungstagebuch**: Mahlzeiten (Frühstück/Mittag/Abend/Snacks), Tagesbilanz für
  Kalorien, Eiweiß, Kohlenhydrate, Zucker, Fett, Ballaststoffe. Der lokale Katalog
  enthält 266 Lebensmittel, darunter 200 zusätzliche Einträge mit Schwerpunkt auf
  Obst, Gemüse, Kräutern, Pilzen und Kartoffeln. Lebensmittel lassen sich außerdem
  über Suche, Barcode-Scan (CameraX + ML Kit, Abgleich über Open Food Facts) oder als eigener Eintrag hinzufügen. Ein Verlauf zeigt zuletzt
  verwendete Lebensmittel und wie oft sie eingetragen wurden. Verbindungs-
  fehler beim Online-Abgleich werden abgefangen; manuelle Eingabe bleibt immer möglich.
- **Ziele-Kalender**: Monatskalender und tägliche Zielbewertung. Einträge werden lokal
  im WebView gespeichert und sind vom Ernährungstagebuch getrennt.
- **Kalorienberechnung**: Grundumsatz (Mifflin-St-Jeor) und Erhaltungskalorien anhand
  Profildaten, mit Kennzeichnung als Näherungswert (`util/CalorieCalculator.kt`).
- **Training**: Protokollierung von Gewicht/Wiederholungen/Sätzen je Übung und
  Muskelgruppe. Ein selbst gezeichnetes Radardiagramm (Brust, Beine, Bauch, Arme,
  Rücken, Schultern) zeigt den Fortschritt der letzten 90 Tage; Gruppen mit zu wenigen
  Daten werden explizit als „noch keine Daten“ markiert statt als 0 % (siehe
  `util/MuscleProgress.kt`).
- **Schlaf**: manuelle Erfassung von Einschlaf- und Aufwachzeit inkl. Verlauf.
- **Streaks**: tägliche Serie bei Erreichen des Kalorienziels, mit aktueller/
  längster Streak und Kalenderübersicht. Tage ohne Eintrag zählen nicht automatisch
  als erreicht (`util/StreakCalculator.kt`).
- Alle Daten werden **lokal auf dem Gerät** in einer Room/SQLite-Datenbank gespeichert.

## Projektstruktur (Auszug)

```
app/src/main/java/de/fitapp/app/
  data/                 Room-Entities, DAOs, Datenbank, Lebensmittel-Repository
  ui/onboarding/        Einrichtungsassistent
  ui/diary/             Ernährungstagebuch, Lebensmittel-Hinzufügen-Dialog, Barcode-Scan
  ui/training/          Trainingsprotokoll
  ui/progress/          Radardiagramm, Streaks, Verläufe
  ui/sleep/             Schlafprotokoll
  ui/profile/           Profil / Bearbeiten aller Angaben
  util/                 Berechnungslogik (Kalorien, Muskelfortschritt, Streaks)
app/src/main/assets/foods_de.json   Lokale Lebensmitteldatenbank (Grundnahrungsmittel)
app/src/main/assets/FOOD_DATA_ATTRIBUTION.txt   Quelle und Lizenz der zusätzlichen BLS-Daten
```

Die 200 ergänzten Nährwertdatensätze stammen aus einem ausgewählten und für die App
auf Makronährwerte reduzierten Ausschnitt des Max Rubner-Instituts (2025),
Bundeslebensmittelschlüssel (BLS) Version 4.0, lizenziert unter CC BY 4.0. Siehe
`app/src/main/assets/FOOD_DATA_ATTRIBUTION.txt` für die vollständige Quellenangabe.

## Voraussetzungen

- Android Studio (aktuell getestet gegen Android Studio Koala/Ladybug oder neuer)
- JDK 17 (wird von aktuellen Android-Studio-Versionen mitgebracht)
- Android SDK mit `compileSdk`/`targetSdk` 34, `minSdk` 26
- Internetverbindung beim ersten Öffnen, damit Gradle die Abhängigkeiten herunterladen
  kann (danach funktioniert die App selbst komplett offline, bis auf den optionalen
  Barcode-Online-Abgleich)

## Einrichtung & Build

1. Projektordner in Android Studio öffnen (**File → Open** → diesen Ordner wählen).
2. Android Studio erkennt automatisch, dass der Gradle-Wrapper-JAR fehlt, und bietet
   an, ihn herunterzuladen bzw. verwendet die eigene Gradle-Installation für den
   ersten Sync. Alternativ, falls lokal Gradle installiert ist:
   ```
   gradle wrapper --gradle-version 8.7
   ```
   damit anschließend `./gradlew` funktioniert.
3. **Gradle Sync** ausführen (geschieht i. d. R. automatisch beim Öffnen).
4. APK bauen:
   ```
   ./gradlew assembleDebug
   ```
   Die fertige APK liegt danach unter `app/build/outputs/apk/debug/app-debug.apk`.
   Für eine signierte Release-APK: `./gradlew assembleRelease` (Signierkonfiguration
   in `app/build.gradle.kts` ergänzen).
5. App auf Emulator oder Gerät ausführen: **Run ▶** in Android Studio, oder
   ```
   ./gradlew installDebug
   ```

## Hinweise zur Barcode-Funktion

Für das Scannen wird die Kamera-Berechtigung zur Laufzeit angefragt. Ohne Berechtigung
oder Kamera (z. B. mancher Emulator) bleibt die manuelle Barcode-Eingabe sowie die
Anlage eines eigenen Lebensmittels als Ausweichmöglichkeit erhalten. Der Online-Abgleich
nutzt die kostenlose Open-Food-Facts-API; bei fehlender Verbindung erscheint ein
verständlicher Hinweis statt eines Absturzes.

## GitHub Actions – automatischer Build

Im Ordner `.github/workflows/android-build.yml` liegt eine CI-Pipeline. Sie installiert
Gradle direkt über `gradle/actions/setup-gradle` (inkl. Caching), sodass sie auch ohne
einen committeten `gradle-wrapper.jar` sofort lauffähig ist. Sobald du den Wrapper-JAR
lokal ergänzt hast (siehe Abschnitt „Bekannte Einschränkungen“), kannst du die
`run: gradle ...`-Zeilen bei Bedarf durch `run: ./gradlew ...` ersetzen.

- **Bei jedem Push/PR auf `main`**: Lint, Unit-Tests und `assembleDebug` laufen
  automatisch; die Debug-APK sowie Test-/Lint-Berichte stehen danach unter dem
  jeweiligen Workflow-Lauf als **Artefakte** zum Download bereit (Tab „Actions“ →
  Lauf auswählen → „Artifacts“).
- **Bei einem Tag im Format `v*`** (z. B. `v1.0.0`): zusätzlich wird eine
  Release-APK gebaut und – falls die unten genannten Secrets hinterlegt sind –
  signiert, als Artefakt hochgeladen und automatisch an ein GitHub Release
  angehängt.

### Einrichtung in GitHub

1. Repository auf GitHub anlegen und dieses Projekt hineinpushen, z. B.:
   ```
   git init
   git add .
   git commit -m "Initial commit: FitApp Android-Projekt"
   git branch -M main
   git remote add origin https://github.com/<dein-benutzername>/<dein-repo>.git
   git push -u origin main
   ```
   Der Workflow läuft danach automatisch bei jedem Push/PR – ganz ohne weitere
   Einrichtung (unsignierte Debug-APK).

2. **Optional – signierte Release-APKs** bei Tags: Lege im Repository unter
   **Settings → Secrets and variables → Actions → New repository secret** folgende
   Secrets an:
   - `RELEASE_KEYSTORE_BASE64` – dein `.jks`/`.keystore`, Base64-kodiert
     (`base64 -w0 release.keystore` bzw. auf macOS `base64 -i release.keystore`)
   - `RELEASE_STORE_PASSWORD` – Keystore-Passwort
   - `RELEASE_KEY_ALIAS` – Alias des Signierschlüssels
   - `RELEASE_KEY_PASSWORD` – Passwort des Schlüssels

   Ohne diese Secrets überspringt der Release-Job die Signierung nicht, sondern
   baut weiterhin eine (unsignierte) Release-APK; für einen Play-Store-tauglichen
   Build sollten die Secrets aber gesetzt sein.

3. Ein Release auslösen:
   ```
   git tag v1.0.0
   git push origin v1.0.0
   ```



- Der Gradle-Wrapper-JAR (`gradle/wrapper/gradle-wrapper.jar`) ist aus dieser Umgebung
  heraus nicht generierbar (kein Internetzugriff), da er ein Binärdownload ist. Android
  Studio ergänzt ihn beim ersten Öffnen automatisch; alternativ lokal `gradle wrapper`
  ausführen (siehe oben). **Wichtig für GitHub Actions:** Der Workflow ruft `./gradlew`
  auf, daher muss der Wrapper-JAR im Repository vorhanden sein, bevor gepusht wird –
  am einfachsten einmal lokal in Android Studio öffnen (generiert ihn automatisch)
  und dann committen, oder `gradle wrapper --gradle-version 8.7` ausführen.
- Diese Umgebung hat kein Android SDK und keinen Netzwerkzugriff, daher konnte der
  Build hier nicht tatsächlich mit `./gradlew assembleDebug` ausgeführt werden. Der
  Code wurde sorgfältig gegen die verwendeten Bibliotheksversionen geschrieben; bitte
  nach dem ersten Öffnen in Android Studio einen Gradle-Sync und Build durchführen und
  mir eventuelle Fehlermeldungen mitteilen, falls welche auftreten sollten.
- Die Radardiagramm-Fortschrittsberechnung ist ein nachvollziehbares, aber bewusst
  einfaches Modell (geschätztes 1-Wiederholungs-Maximum, relative Veränderung); für
  fortgeschrittenere Trainingsanalysen ließe sich das weiter verfeinern.
