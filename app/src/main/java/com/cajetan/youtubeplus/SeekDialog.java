package com.cajetan.youtubeplus;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class SeekDialog extends DialogFragment {

    View layoutView;

    NumberPicker secondPicker;
    NumberPicker minutePicker;
    NumberPicker hourPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog.Builder builder = new TimePickerDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        layoutView = inflater.inflate(R.layout.dialog_seek, null);

        builder.setView(layoutView);

        // TODO: get durations according to the video
        secondPicker = layoutView.findViewById(R.id.second_picker);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        minutePicker = layoutView.findViewById(R.id.minute_picker);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        hourPicker = layoutView.findViewById(R.id.hour_picker);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(100);

        return builder.create();
    }

}
