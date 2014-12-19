package rectangledbmi.com.pittsburghrealtimetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by rgupta on 12/18/14.
 */
public class BusInformationDialog extends DialogFragment {

    String message;
    String title;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_BusDialogTransparent);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_information_dialog, null);
        builder.setView(view);

        Button negBut = (Button) view.findViewById(R.id.info_dismiss);
        negBut.setBackgroundColor(getResources().getColor(R.color.blue_500_trans));
        negBut.setTextColor(Color.WHITE);
        negBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setTitle(title);

        TextView text = (TextView) view.findViewById(R.id.info_text);
        text.setText(message);
        text.setTextColor(getResources().getColor(R.color.orange_600));

        final Dialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.Theme_BusDialogTransparent;
        return dialog;
    }

}
