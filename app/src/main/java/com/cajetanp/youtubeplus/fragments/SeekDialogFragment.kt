package com.cajetanp.youtubeplus.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import com.cajetanp.youtubeplus.R
import java.lang.ClassCastException

class SeekDialogFragment : DialogFragment() {

    private lateinit var mListener: SeekDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        val layoutView: View = activity?.layoutInflater!!.inflate(R.layout.dialog_seek, null)
        builder.setView(layoutView)

        var currentSecond = arguments!!.getInt("current_second")
        var currentMinute = Math.floor(currentSecond / 60.0).toInt()
        val currentHour = Math.floor(currentMinute / 60.0).toInt()

        currentMinute -= currentHour*60
        currentSecond -= currentHour*60*60
        currentSecond -= currentMinute*60

        // TODO: investigate what's going on with that last item
        val durationParts: List<String> = arguments?.getString("duration_string")!!
                .substring(2).replace('M', ' ').replace('S', ' ')
                .replace('H', ' ').split(" ").dropLast(1)

        val maxSeconds: Int
        val maxMinutes: Int
        val maxHours: Int

        when (durationParts.size) {
            3 -> {
                maxHours = durationParts[0].toInt()
                maxMinutes = 59
                maxSeconds = 59
            }
            2 -> {
                maxHours = 0
                maxMinutes = durationParts[0].toInt()
                maxSeconds = 59
            }
            else -> {
                maxHours = 0
                maxMinutes = 0
                maxSeconds = durationParts[0].toInt()
            }
        }

        val secondPicker: NumberPicker = layoutView.findViewById(R.id.second_picker)
        secondPicker.minValue = 0
        secondPicker.maxValue = maxSeconds
        secondPicker.value = currentSecond

        val minutePicker: NumberPicker = layoutView.findViewById(R.id.minute_picker)
        minutePicker.minValue = 0
        minutePicker.maxValue = maxMinutes
        minutePicker.value = currentMinute

        if (maxMinutes == 0)
            minutePicker.visibility = View.GONE

        val hourPicker: NumberPicker = layoutView.findViewById(R.id.hour_picker)
        hourPicker.minValue = 0
        hourPicker.maxValue = maxHours
        hourPicker.value = currentHour

        if (maxHours == 0)
            hourPicker.visibility = View.GONE

        val confirmButton: Button = layoutView.findViewById(R.id.confirm_button) as Button
        confirmButton.setOnClickListener {
            val duration = hourPicker.value * 60 * 60 +
                    minutePicker.value * 60 + secondPicker.value

            mListener.onSeekButtonClicked(duration.toFloat())
            dismiss()
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is SeekDialogListener)
            mListener = context
        else
            throw ClassCastException(activity.toString() + " must implement SeekDialogListener")
    }

    interface SeekDialogListener {
        fun onSeekButtonClicked(duration: Float)
    }
}
