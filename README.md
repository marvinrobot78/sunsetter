# Sunsetter

A Wear OS complication app that displays today's **sunrise** and **sunset** times on your watch face.

## Features

- Shows sunrise and sunset times as arc (`RANGED_VALUE`) complications
- Arc fill reflects how far through the day the event is (sunrise arc fills toward noon; sunset arc fills toward midnight)
- Calculates sun times from your device's GPS location
- Caches the result daily — recalculates only once per day or when your location changes
- Graceful fallbacks: defaults to 6:00 AM / 6:00 PM when no location is available

## Requirements

- Wear OS 3 (API 30+)
- Location permission (`ACCESS_FINE_LOCATION`)

## Setup

1. Install the app on your watch.
2. Open the app and grant location permission when prompted.
3. Add the **Sunrise** or **Sunset** complication to a watch face via the watch face editor.

## How it works

`MainActivity` obtains a GPS fix via `FusedLocationProviderClient` and stores the coordinates in `SharedPreferences`. Each complication service reads those coordinates through `SunTimeRepository`, which uses [commons-suncalc](https://shredzone.org/maven/commons-suncalc/) to compute rise/set times and caches them for the day. The complication values update at most once per hour (per the manifest `UPDATE_PERIOD_SECONDS`), but sun-time recalculation only happens once per day.

## Dependencies

| Library | Purpose |
|---|---|
| `androidx.wear.watchface:watchface-complications-data-source-ktx` | Complication data source API |
| `com.google.android.gms:play-services-location` | FusedLocationProviderClient |
| `org.shredzone.commons:commons-suncalc` | Sunrise/sunset calculation |
| `androidx.wear:wear` | BoxInsetLayout for round/square watch support |