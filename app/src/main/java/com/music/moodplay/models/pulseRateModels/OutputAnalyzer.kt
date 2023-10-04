package com.music.moodplay.models.pulseRateModels

import android.app.Activity
import android.view.TextureView
import android.os.CountDownTimer
import com.music.moodplay.R
import com.music.moodplay.presentation.utils.CameraService
import com.music.moodplay.presentation.utils.ChartDrawer
import com.music.moodplay.presentation.utils.PulseRateHandler
import com.music.moodplay.presentation.utils.StaticFields
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class OutputAnalyzer(
    private val activity: Activity,
    graphTextureView: TextureView,
    private val pulseRateHandler: PulseRateHandler
) {
    private var chartDrawer: ChartDrawer = ChartDrawer(graphTextureView)
    private var store: MeasureStore? = null
    private val measurementInterval = 45
    private val measurementLength = 15000 // ensure the number of data points is the power of two
    private val clipLength = 3500
    private var detectedValleys = 0
    private var ticksPassed = 0
    private val valleys = CopyOnWriteArrayList<Long>()
    private var timer: CountDownTimer? = null

    private fun detectValley(): Boolean {
        val valleyDetectionWindowSize = 13
        val subList = store!!.getLastStdValues(valleyDetectionWindowSize)
        return if (subList.size < valleyDetectionWindowSize) {
            false
        } else {
            val referenceValue =
                subList[Math.ceil((valleyDetectionWindowSize / 2f).toDouble()).toInt()].measurement
            for (measurement in subList) {
                if (measurement.measurement < referenceValue) return false
            }

            // filter out consecutive measurements due to too high measurement rate
            subList[Math.ceil((valleyDetectionWindowSize / 2f).toDouble())
                .toInt()].measurement != subList[Math.ceil((valleyDetectionWindowSize / 2f).toDouble())
                .toInt() - 1].measurement
        }
    }

    fun measurePulse(textureView: TextureView, cameraService: CameraService) {

        // 20 times a second, get the amount of red on the picture.
        // detect local minimums, calculate pulse.
        store = MeasureStore()
        detectedValleys = 0
        timer = object : CountDownTimer(measurementLength.toLong(), measurementInterval.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                // skip the first measurements, which are broken by exposure metering
                if (clipLength > ++ticksPassed * measurementInterval) return
                val thread = Thread {
                    val currentBitmap = textureView.bitmap
                    val pixelCount = textureView.width * textureView.height
                    var measurement = 0
                    val pixels = IntArray(pixelCount)
                    currentBitmap!!.getPixels(
                        pixels,
                        0,
                        textureView.width,
                        0,
                        0,
                        textureView.width,
                        textureView.height
                    )

                    // extract the red component
                    // https://developer.android.com/reference/android/graphics/Color.html#decoding
                    for (pixelIndex in 0 until pixelCount) {
                        measurement += pixels[pixelIndex] shr 16 and 0xff
                    }
                    // max int is 2^31 (2147483647) , so width and height can be at most 2^11,
                    // as 2^8 * 2^11 * 2^11 = 2^30, just below the limit
                    store!!.add(measurement)
                    if (detectValley()) {
                        detectedValleys += 1
                        valleys.add(store!!.lastTimestamp.time)
                        // in 13 seconds (13000 milliseconds), I expect 15 valleys. that would be a pulse of 15 / 130000 * 60 * 1000 = 69
                        val currentValue = String.format(
                            Locale.getDefault(),
                            activity.getResources().getQuantityString(
                                R.plurals.measurement_output_template,
                                detectedValleys
                            ),
                            if (valleys.size == 1) 60f * detectedValleys / Math.max(
                                1f,
                                (measurementLength - millisUntilFinished - clipLength) / 1000f
                            ) else 60f * (detectedValleys - 1) / Math.max(
                                1f,
                                (valleys[valleys.size - 1] - valleys[0]) / 1000f
                            ),
                            detectedValleys,
                            1f * (measurementLength - millisUntilFinished - clipLength) / 1000f
                        )
                        sendMessage(StaticFields.MESSAGE_UPDATE_REALTIME, currentValue)
                    }

                    // draw the chart on a separate thread.
                    val chartDrawerThread = Thread {
                        chartDrawer.draw(
                            store!!.stdValues
                        )
                    }
                    chartDrawerThread.start()
                }
                thread.start()
            }

            override fun onFinish() {
                val stdValues = store!!.stdValues

                // clip the interval to the first till the last one - on this interval, there were detectedValleys - 1 periods

                // If the camera only provided a static image, there are no valleys in the signal.
                // A camera not available error is shown, which is the most likely cause.
                if (valleys.size == 0) {
                    pulseRateHandler.updatingData(
                        StaticFields.MESSAGE_CAMERA_NOT_AVAILABLE,
                        "No valleys detected - there may be an issue when accessing the camera.",
                        0.0F
                    )
                    return
                }
                val currentValue = String.format(
                    Locale.getDefault(),
                    activity.getResources().getQuantityString(
                        R.plurals.measurement_output_template,
                        detectedValleys - 1
                    ),
                    60f * (detectedValleys - 1) / Math.max(
                        1f,
                        (valleys[valleys.size - 1] - valleys[0]) / 1000f
                    ),
                    detectedValleys - 1,
                    1f * (valleys[valleys.size - 1] - valleys[0]) / 1000f
                )
                sendMessage(StaticFields.MESSAGE_UPDATE_REALTIME, currentValue)
                val returnValueSb = StringBuilder()
                returnValueSb.append(currentValue)
                returnValueSb.append(activity.getString(R.string.row_separator))

                returnValueSb.append(activity.getString(R.string.raw_values))
                returnValueSb.append(activity.getString(R.string.row_separator))
                for (stdValueIdx in stdValues.indices) {
                    // stdValues.forEach((value) -> { // would require API level 24 instead of 21.
                    val value = stdValues[stdValueIdx]
                    val timeStampString = SimpleDateFormat(
                        activity.getString(R.string.dateFormatGranular),
                        Locale.getDefault()
                    ).format(value.timestamp)
                    returnValueSb.append(timeStampString)
                    returnValueSb.append(activity.getString(R.string.separator))
                    returnValueSb.append(value.measurement)
                    returnValueSb.append(activity.getString(R.string.row_separator))
                }
                returnValueSb.append(activity.getString(R.string.output_detected_peaks_header))
                returnValueSb.append(activity.getString(R.string.row_separator))

                // add detected valleys location
                for (tick in valleys) {
                    returnValueSb.append(tick)
                    returnValueSb.append(activity.getString(R.string.row_separator))
                }
                sendMessage(StaticFields.MESSAGE_UPDATE_FINAL, returnValueSb.toString(),
                    60f * (detectedValleys - 1) / Math.max(
                    1f,
                    (valleys[valleys.size - 1] - valleys[0]) / 1000f
                ))
                cameraService.stop()
            }
        }
        timer?.start()
    }

    fun stop() {
        if (timer != null) {
            timer!!.cancel()
        }
    }

    fun sendMessage(what: Int, message: String, data: Float = 0.0F) {
        pulseRateHandler.updatingData(what, message, data)
    }
}