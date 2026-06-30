package com.atkinson.sunsetter

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class SunriseComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val sunTimes = SunTimeRepository(this).getSunTimesForToday()
        return buildComplicationData(
            value = HALF_DAY - sunTimes.sunriseMinute,
            text = minuteOfDayToTimeString(sunTimes.sunriseMinute)
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        buildComplicationData(value = HALF_DAY - 370f, text = "6:10 AM")

    private fun buildComplicationData(value: Float, text: String) =
        RangedValueComplicationData.Builder(
            value = value,
            min = 0f,
            max = HALF_DAY,
            contentDescription = PlainComplicationText.Builder("Sunrise time").build()
        )
            .setText(PlainComplicationText.Builder(text).build())
            .setTitle(PlainComplicationText.Builder("Sunrise").build())
            .build()
}
