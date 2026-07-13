# Pico SDK — Platform Parity Report

## Summary

Comparison of the native SDK methods and callbacks provided by the iOS SDK (`PicoSDK`) and the Android SDK (`PicoIO.aar`). This document only covers what the vendor SDKs expose — not our Cordova plugin layer.

---

## SDK Methods

### CUPico (iOS SDK)

| Method | Description |
|---|---|
| `sendLabDataRequest` | Request LAB color data |
| `sendSensorDataRequest` | Request sensor data (R, G, B integers) |
| `sendRawDataRequest` | Request raw data with per-LED firing |
| `sendCalibrationRequest` | Calibrate the sensor |
| `sendBatteryLevelRequest` | Get battery level (returns BOOL) |
| `sendBatteryStatusRequest` | Get battery status (returns BOOL) |
| `disconnect` | Disconnect from Pico |
| `discoverServices` | Discover BLE services |
| `setScanRawAdjustment:b:c:` | Set raw scan adjustment values (3 floats) |
| `state` | Get current connection state (`CUPicoState`) |

### Pico (Android SDK — PicoIO.aar)

| Method | Description |
|---|---|
| `sendLabDataRequest` | Request LAB color data |
| `sendSensorDataRequest` | Request sensor data (R, G, B integers) |
| `sendRawDataRequest` | Request raw data with per-LED firing (**added in PicoIO-v2**) |
| `sendCalibrationRequest` | Calibrate the sensor |
| `sendBatteryLevelRequest` | Get battery level |
| `sendBatteryStatusRequest` | Get battery status |
| `disconnect` | Disconnect from Pico |
| `setScanRawAdjustment` | Set raw scan adjustment values |
| `readFirmwareVersion` | Read firmware revision |
| `setListener` | Set PicoListener |
| `setGf` | Set GF value |
| `close` | Close GATT connection |

---

## SDK Callbacks / Delegates

### CUPicoDelegate (iOS SDK)

| Callback | Description |
|---|---|
| `onConnectSuccess:` | Connection succeeded |
| `onConnectFail:` | Connection failed |
| `onDisconnect:error:` | Device disconnected |
| `onFetchLabData:lab:` | LAB data received (`CULAB`) |
| `onFetchSensorData:sensorData:` | Sensor data received (`CUSensorData` — R, G, B) |
| `onFetchRawData:rawData:` | **Raw data received (`NSArray` — per-LED readings)** |
| `onCalibrationComplete:success:` | Calibration result (BOOL) |
| `onFetchBatteryLevel:level:` | Battery level received (NSInteger) |
| `onFetchBatteryStatus:status:` | Battery status received (`CUBatteryStatus`) |

### CUPicoConnectorDelegate (iOS SDK)

| Callback | Description |
|---|---|
| `onConnectSuccess:` | Connection succeeded (returns `CUPico`) |
| `onConnectFail:` | Connection failed (returns `NSError`) |

### PicoListener (Android SDK)

| Callback | Description |
|---|---|
| `onCalibrationComplete` | Calibration result (`Pico.CalibrationResult`) |
| `onDisconnect` | Device disconnected |
| `onFetchBatteryLevel` | Battery level received (int) |
| `onFetchBatteryStatus` | Battery status received (`Pico.BatteryStatus`) |
| `onFetchLabData` | LAB data received (`LAB`) |
| `onFetchSensorData` | Sensor data received (`SensorData` — R, G, B) |
| `onFetchRawData` | Raw data received (`int[]` — 9 values: 3 LEDs x 3 sensor channels) (**added in PicoIO-v2**) |

### PicoConnectorListener (Android SDK)

| Callback | Description |
|---|---|
| `onConnectSuccess` | Connection succeeded (returns `Pico`) |
| `onConnectFail` | Connection failed (`PicoError`) |

---

## SDK Data Classes

### iOS SDK

| Class | Properties |
|---|---|
| `CULAB` | `l`, `a`, `b` (double) — `distance:`, `distanceSquared:`, `color`, `toString` |
| `CUSensorData` | `r`, `g`, `b` (NSInteger) — `toString` |
| `CUSwatch` | `name`, `code` (NSString), `lab` (CULAB) |
| `CUMatch` | `swatch` (CUSwatch), `distance`, `distanceSquared` (double) |
| `CUSwatchMatcher` | `getMatch:swatches:`, `getMatches:swatches:numMatches:` |
| `CUPicoConnector` | `connect`, `cancelConnect` |

### Android SDK

| Class | Properties / Methods |
|---|---|
| `LAB` | `l`, `a`, `b` — `getColor`, `getDistance`, `getDistanceSquared` |
| `SensorData` | `r`, `g`, `b` |
| `Swatch` | `name`, `code`, `lab` — `getName`, `getCode`, `getLab` |
| `Match` | `swatch`, `distanceSquared` — `getSwatch`, `getDistance`, `getDistanceSquared` |
| `SwatchMatcher` | `getMatch`, `getMatches`, `bestMatch`, `numMatches` |
| `RawAdjustment` | Raw scan adjustment values |
| `PicoConnector` | `getInstance`, `connect`, `cancelConnect`, `setListener` |
| `CommandQueue` | Internal BLE command queue management |
| `UARTCommand` | Base UART BLE command |
| `LabDataCommand` | LAB data request command |
| `LabDataNotification` | LAB data notification handler |
| `SensorDataCommand` | Sensor data request command |
| `BatteryLevelCommand` | Battery level request command |
| `BatteryStatusCommand` | Battery status request command |
| `BatteryStatusNotification` | Battery status notification handler |
| `CalibrationCommand` | Calibration request command |
| `MultiRequestCommand` | Multi-stage request command |

---

## Feature Gaps

### iOS has, Android missing

*No known gaps as of PicoIO-v2.*

Previously missing (now resolved in PicoIO-v2):

| Feature | iOS | Android |
|---|---|---|
| `sendRawDataRequest` | Native method — fires each LED individually, reads all 3 sensor channels per LED | Added in PicoIO-v2 |
| `onFetchRawData` callback | Returns `NSArray` with per-LED raw sensor readings (9 values: 3 LEDs x 3 sensors) | Added in PicoIO-v2 — `int[]` with 9 values |

### Android has, iOS missing

| Feature | Android | iOS |
|---|---|---|
| `readFirmwareVersion` | Reads firmware revision string | Not exposed in public headers |
| `setGf` | Sets GF value on Pico | Not exposed in public headers |
| `close` | Explicit GATT close | Not exposed (handled internally) |
| Internal command classes | `CommandQueue`, `UARTCommand`, individual command classes exposed | Not exposed — SDK is opaque |

### Shared (parity)

| Feature | iOS | Android |
|---|---|---|
| `sendLabDataRequest` | Yes | Yes |
| `sendSensorDataRequest` | Yes | Yes |
| `sendCalibrationRequest` | Yes | Yes |
| `sendBatteryLevelRequest` | Yes | Yes |
| `sendBatteryStatusRequest` | Yes | Yes |
| `disconnect` | Yes | Yes |
| `setScanRawAdjustment` | Yes (3 float params) | Yes |
| Swatch matching | `CUSwatchMatcher` | `SwatchMatcher` |
| Color data models | `CULAB`, `CUSensorData`, `CUSwatch`, `CUMatch` | `LAB`, `SensorData`, `Swatch`, `Match` |

---

## Key Takeaway

As of PicoIO-v2, the critical gap is closed. Both platforms now support `sendRawDataRequest` / `onFetchRawData`, providing the full 3×3 raw sensor matrix (3 LEDs × 3 sensor channels = 9 values). The previous Android workaround (LAB→RGB math synthesis with zeroed off-diagonal values) has been replaced by the native SDK call.
