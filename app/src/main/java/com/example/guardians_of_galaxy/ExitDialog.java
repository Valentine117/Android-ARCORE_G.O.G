package com.example.guardians_of_galaxy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

public class ExitDialog extends Dialog {

    public Button exitBtn, cancelBtn;

    public ExitDialog(@NonNull Context context,PlayActivity pa) {
        super(context);
        setContentView(R.layout.exit_dialog);

        exitBtn = (Button) findViewById(R.id.exitBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pa.finish();
                Intent intent = new Intent(pa.getApplicationContext(), MainActivity.class);
                pa.startActivity(intent);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
