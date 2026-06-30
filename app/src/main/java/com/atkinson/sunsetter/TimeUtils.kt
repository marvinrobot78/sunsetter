package com.atkinson.sunsetter

internal const val HALF_DAY = 720f

internal fun minuteOfDayToTimeString(minute: Int): String {
    val hour = minute / 60
    val min = minute % 60
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, min, amPm)
}
