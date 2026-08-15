# Volumer

Volumer is an Android MVP for context-aware media volume control.

## What it does

- Keeps your volume unchanged while you remain in a private/quiet place.
- Detects likely crowded/public context from nearby Bluetooth LE device density.
- Lowers media volume to a configurable public level (20% by default).
- Remembers the volume from before Volumer adjusted it.
- Restores that volume when returning to quiet/private context, with a configurable minimum (45% by default).
- Lets you save your current location as a private place using Android geofencing.
- Runs automatic monitoring as an explicit foreground service with an ongoing notification.

## Privacy

Volumer does not record microphone audio. Nearby BLE scan results are reduced to a local device count and are not uploaded by the app.

## Build

The repository contains a native Android project using Kotlin and Jetpack Compose. GitHub Actions builds a debug APK and uploads it as an artifact.
