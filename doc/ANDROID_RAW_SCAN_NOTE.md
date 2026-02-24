# Android Raw Scan Note (short)

## What was changed

- Android raw scan now uses direct sensor data again (`sendSensorDataRequest`) instead of LAB->RGB->synthetic raw conversion.
- The plugin emits both events for compatibility: `rawScan` and `sensorData`.

## What the issue was

- The newer Android path generated synthetic raw values from LAB, which changed value behavior and could break existing app logic that expected hardware-near sensor values.
- iOS has native raw scan support (`sendRawDataRequest`), Android SDK behavior differs.

## Functionality now (short)

- `scanRaw` on Android returns direct sensor-based values.
- `rawScan` remains available for raw-map consumers.
- `sensorData` remains available for legacy consumers in uvanalyzer.

## Deutsch (3 Sätze)

Früher hat Android die Raw-Werte praktisch direkt aus den Sensorwerten geliefert, später wurden sie aus LAB-Werten künstlich berechnet. Genau das war schwierig, weil iOS echtes Raw nativ unterstützt, Android aber im SDK ein anderes Verhalten hat und deshalb die Werte nicht 1:1 vergleichbar waren. Jetzt nutzt Android wieder direkte Sensorwerte und sendet gleichzeitig `rawScan` und `sensorData`, damit alte und neue App-Logik parallel funktionieren.

## Commit-Info (LAB-Umstellung)

- Eingeführt in Commit: `a365ae375d848d661a1c43c73a3deb530b90b2fe`
- Commit-Message: `feat: update version to 1.0.4 and add raw scan functionality in PicoPlugin`
- Datum: `14.01.2026, 12:14:13 +0100`

## Timeline (kurz)

- `02.01.2021` (`62edb26...`): Android lieferte praktisch direkte Sensorwerte über `sensorData`; kein eigener nativer iOS-äquivalenter Raw-Request-Pfad.
- `19.12.2025` (`03c90c0...`): Permission-/Connect-Refactor (kein neuer Raw-Algorithmus).
- `14.01.2026` (`a365ae3...`): Einführung von `scanRaw`; wegen unterschiedlichem SDK-Verhalten (iOS hat `sendRawDataRequest`, Android nicht gleichwertig) wurde ein LAB->RGB->synthetic-raw Workaround gebaut, um ein einheitliches `rawScan`-Format zu liefern.
- `24.02.2026` (lokale Anpassung): Rückbau auf direkte Android-Sensordaten für `scanRaw` + paralleles Senden von `rawScan` und `sensorData` für Kompatibilität.

## Warum das „komisch“ wirkte

Der LAB->raw-Schritt war ein API-/Format-Workaround, nicht die sauberste Messdatenquelle. Er hat geholfen, iOS- und Android-Events schnell anzugleichen, konnte aber Messwerte verfälschen und bestehende Android-Auswertung brechen. Deshalb wurde jetzt wieder auf direkte Sensorwerte zurückgestellt.
