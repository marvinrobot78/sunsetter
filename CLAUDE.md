
1# Sunsetter

A Wear OS complication data source app that provides sunrise and sunset times as `RANGED_VALUE` complications.

## Project structure

```
app/src/main/java/com/atkinson/sunsetter/
├── MainActivity.kt                        # Requests location permission; stores lat/lng
├── SunTimeRepository.kt                   # Sun time calculation and daily caching
├── SunriseComplicationDataSourceService.kt
├── SunsetComplicationDataSourceService.kt
└── TimeUtils.kt                           # minuteOfDayToTimeString(), MINUTES_IN_DAY
```

## Architecture

- **Package:** `com.atkinson.sunsetter`
- **Min SDK:** 30 (Wear OS 3)
- **Language:** Kotlin

### Data flow

1. `MainActivity` requests `ACCESS_FINE_LOCATION`, obtains the device's last known (or current) GPS fix via `FusedLocationProviderClient`, and stores lat/lng as `Float` in `SharedPreferences` (`sunsetter_prefs`).
2. On location save, `MainActivity` calls `ComplicationDataSourceUpdateRequester.requestUpdateAll()` on both services to force an immediate refresh.
3. Each complication service calls `SunTimeRepository.getSunTimesForToday()`, which returns cached values if the stored date matches today, or recalculates using `commons-suncalc` and caches the result.
4. Sun times are expressed as minutes, but with different reference points and ranges:
   - **Sunrise:** minutes until midday (e.g. 6:10 AM → 720 - 370 = 350), range 0–720 (a full arc means sunrise at midnight; empty arc means sunrise at noon)
   - **Sunset:** minutes since midday (e.g. 5:10 PM → 310), range 0–720 (noon to midnight)
   - Both use the constant `HALF_DAY = 720f` from `TimeUtils.kt`
   - The displayed text is always the human-readable local time (e.g. "5:10 PM"), not the offset value

### Caching strategy

`SunTimeRepository` stores four keys in `SharedPreferences`:
- `cached_date` — ISO date string (`yyyy-MM-dd`) in the device's local timezone
- `sunrise_minute` / `sunset_minute` — cached results
- `has_location` — whether a real GPS fix has ever been stored

The cache is invalidated (by removing `cached_date`) when a new location is stored, so the next complication update recalculates with the new position.

### Update period

Both services declare `UPDATE_PERIOD_SECONDS=3600` in the manifest. The system calls each service at most once per hour, but the repository's date cache means `commons-suncalc` only runs once per day.

### Fallbacks

- No location yet → returns 360 (6:00 AM) / 1080 (6:00 PM)
- Polar region (null rise/set from `commons-suncalc`) → same defaults
- Any exception during calculation → same defaults

## Key dependencies

| Dependency | Purpose |
|---|---|
| `androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1` | Complication data source API |
| `com.google.android.gms:play-services-location:21.3.0` | `FusedLocationProviderClient` |
| `org.shredzone.commons:commons-suncalc:3.9` | Sunrise/sunset calculation (zero transitive deps) |
| `androidx.wear:wear:1.3.0` | `BoxInsetLayout` for round/square watch support |

## First-run flow

1. Install and open the app on the watch
2. Grant location permission when prompted
3. Add the **Sunrise** or **Sunset** complication to a watch face via the watch face editor
