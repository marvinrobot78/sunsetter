package com.atkinson.sunsetter

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class SunsetComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val sunTimes = SunTimeRepository(this).getSunTimesForToday()
        return buildComplicationData(
            value = (sunTimes.sunsetMinute - HALF_DAY).toFloat(),
            text = minuteOfDayToTimeString(sunTimes.sunsetMinute)
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        buildComplicationData(value = 390f, text = "6:30 PM")

    private fun buildComplicationData(value: Float, text: String) =
        RangedValueComplicationData.Builder(
            value = value,
            min = 0f,
            max = HALF_DAY,
            contentDescription = PlainComplicationText.Builder("Sunset time").build()
        )
            .setText(PlainComplicationText.Builder(text).build())
            .setTitle(PlainComplicationText.Builder("Sunset").build())
            .build()
}
