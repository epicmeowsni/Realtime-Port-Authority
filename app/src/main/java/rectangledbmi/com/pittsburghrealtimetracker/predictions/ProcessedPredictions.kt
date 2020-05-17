package rectangledbmi.com.pittsburghrealtimetracker.predictions

import android.os.Bundle

import com.google.android.gms.maps.model.Marker

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.predictions.PredictionsType
import com.rectanglel.patstatic.predictions.PredictionsView
import com.rectanglel.patstatic.predictions.response.Prd
import com.rectanglel.patstatic.vehicles.response.Vehicle

import rectangledbmi.com.pittsburghrealtimetracker.ui.MainActivity

/**
 *
 * Holds prediction info for the [PredictionsView].
 *
 * Created by epicstar on 10/14/16.
 * @author Jeremy Jao
 * @since 78
 */

class ProcessedPredictions private constructor(val marker: Marker, predictionsType: PredictionsType, predictions: List<Prd>) {
    val predictions: String

    init {
        this.predictions = processPrds(predictionsType, predictions)
    }// predictionstype is already in the marker but there's an IllegalStateException if not on main thread when running marker.getTag()...

    companion object {

        /**
         * This is the date format to print
         *
         * @since 46
         */
        private val DATE_FORMAT_PRINT = "hh:mm a"

        /**
         * The default date format to parse... The timezone is set as EST in
         * [MainActivity.onCreate]
         * @since 46
         */
        private val dateFormat = SimpleDateFormat(DATE_FORMAT_PRINT, Locale.US)

        init {
            dateFormat.timeZone = TimeZone.getTimeZone("America/New_York")
        }

        @JvmStatic
        fun create(marker: Marker, predictionsType: PredictionsType, predictions: List<Prd>): ProcessedPredictions {
            return ProcessedPredictions(marker, predictionsType, predictions)
        }

        private fun processPrds(predictionsType: PredictionsType, prds: List<Prd>): String {
            val str = prds.joinToString(separator = "\n", transform = {
                when (predictionsType) {
                    is Pt -> "${it.rt} (${it.vid}): ${dateFormat.format(it.prdtm)}"
                    is Vehicle -> "(${it.stpid}) ${it.stpnm}: ${dateFormat.format(it.prdtm)}"
                    else -> ""
                }
            })
            return if (str.isNotEmpty()) str else "No predictions available."
        }
    }
}
