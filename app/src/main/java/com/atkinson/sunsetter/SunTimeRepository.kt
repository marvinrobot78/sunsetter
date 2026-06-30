package com.atkinson.sunsetter

import android.content.Context
import android.content.SharedPreferences
import org.shredzone.commons.suncalc.SunTimes as SunCalc
import java.time.LocalDate
import java.time.ZoneId

class SunTimeRepository(private val context: Context) {

    data class SunTimes(val sunriseMinute: Int, val sunsetMinute: Int)

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSunTimesForToday(): SunTimes {
        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        if (prefs.getString(KEY_CACHED_DATE, null) == today) {
            return SunTimes(
                prefs.getInt(KEY_SUNRISE_MINUTE, DEFAULT_SUNRISE),
                prefs.getInt(KEY_SUNSET_MINUTE, DEFAULT_SUNSET)
            )
        }
        return calculateAndCache(today)
    }

    private fun calculateAndCache(today: String): SunTimes {
        if (!prefs.getBoolean(KEY_HAS_LOCATION, false)) {
            return SunTimes(DEFAULT_SUNRISE, DEFAULT_SUNSET)
        }
        val lat = prefs.getFloat(KEY_LAT, 0f).toDouble()
        val lng = prefs.getFloat(KEY_LNG, 0f).toDouble()

        return try {
            val times = SunCalc.compute()
                .at(lat, lng)
                .on(LocalDate.now(ZoneId.systemDefault()))
                .timezone(ZoneId.systemDefault())
                .execute()

            val sunriseMinute = times.rise?.let { it.hour * 60 + it.minute } ?: DEFAULT_SUNRISE
            val sunsetMinute = times.set?.let { it.hour * 60 + it.minute } ?: DEFAULT_SUNSET

            prefs.edit()
                .putString(KEY_CACHED_DATE, today)
                .putInt(KEY_SUNRISE_MINUTE, sunriseMinute)
                .putInt(KEY_SUNSET_MINUTE, sunsetMinute)
                .apply()

            SunTimes(sunriseMinute, sunsetMinute)
        } catch (e: Exception) {
            SunTimes(DEFAULT_SUNRISE, DEFAULT_SUNSET)
        }
    }

    fun storeLocation(latitude: Double, longitude: Double) {
        prefs.edit()
            .putBoolean(KEY_HAS_LOCATION, true)
            .putFloat(KEY_LAT, latitude.toFloat())
            .putFloat(KEY_LNG, longitude.toFloat())
            .remove(KEY_CACHED_DATE) // invalidate cache so sun times recalculate with new location
            .apply()
    }

    companion object {
        const val PREFS_NAME = "sunsetter_prefs"
        private const val KEY_CACHED_DATE = "cached_date"
        private const val KEY_SUNRISE_MINUTE = "sunrise_minute"
        private const val KEY_SUNSET_MINUTE = "sunset_minute"
        private const val KEY_HAS_LOCATION = "has_location"
        private const val KEY_LAT = "latitude"
        private const val KEY_LNG = "longitude"
        const val DEFAULT_SUNRISE = 360  // 6:00 AM fallback
        const val DEFAULT_SUNSET = 1080  // 6:00 PM fallback
    }
}
