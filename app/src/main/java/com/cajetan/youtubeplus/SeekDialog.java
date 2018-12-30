package com.cajetan.youtubeplus;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class SeekDialog extends DialogFragment {

    View layoutView;

    NumberPicker secondPicker;
    NumberPicker minutePicker;
    NumberPicker hourPicker;

    Button confirmButton;

    SeekDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog.Builder builder = new TimePickerDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        layoutView = inflater.inflate(R.layout.dialog_seek, null);

        builder.setView(layoutView);

        int currentSecond = getArguments().getInt("current_second");
        int currentMinute = (int) Math.floor(currentSecond / 60);
        int currentHour = (int) Math.floor(currentMinute / 60);

        currentMinute -= currentHour*60;
        currentSecond -= currentHour*60*60;
        currentSecond -= currentMinute*60;

        String[] durationParts = getArguments().getString("duration_string")
                .substring(2).replace('M', ' ').replace('S', ' ')
                .replace('H', ' ').split(" ");

        int maxSeconds;
        int maxMinutes;
        int maxHours;

        if (durationParts.length == 3) {
            maxHours = Integer.parseInt(durationParts[0]);
            maxMinutes = 59;
            maxSeconds = 59;
        } else if (durationParts.length == 2) {
            maxHours = 0;
            maxMinutes = Integer.parseInt(durationParts[0]);
            maxSeconds = 59;
        } else {
            maxHours = 0;
            maxMinutes = 0;
            maxSeconds = Integer.parseInt(durationParts[0]);
        }

        secondPicker = layoutView.findViewById(R.id.second_picker);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(maxSeconds);
        secondPicker.setValue(currentSecond);

        minutePicker = layoutView.findViewById(R.id.minute_picker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(maxMinutes);
        minutePicker.setValue(currentMinute);

        if (maxMinutes == 0)
            minutePicker.setVisibility(View.GONE);

        hourPicker = layoutView.findViewById(R.id.hour_picker);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(maxHours);
        hourPicker.setValue(currentHour);

        if (maxHours == 0)
            hourPicker.setVisibility(View.GONE);

        confirmButton = layoutView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float duration = hourPicker.getValue()*60*60 +
                        minutePicker.getValue()*60 + secondPicker.getValue();

                mListener.onSeekButtonClicked(duration);
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SeekDialogListener) {
            mListener = (SeekDialogListener) context;
        } else {
            throw new ClassCastException(getActivity().toString() +
                    " must implement SeekDialogListener");
        }
    }

    public interface SeekDialogListener {
        void onSeekButtonClicked(float duration);
    }
}
