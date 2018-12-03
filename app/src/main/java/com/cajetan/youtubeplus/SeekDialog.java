package com.cajetan.youtubeplus;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
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

        confirmButton = layoutView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: maybe convert here

                mListener.onSeekButtonClicked(hourPicker.getValue(), minutePicker.getValue(),
                        secondPicker.getValue());

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
        void onSeekButtonClicked(int hours, int minutes, int seconds);
    }
}
