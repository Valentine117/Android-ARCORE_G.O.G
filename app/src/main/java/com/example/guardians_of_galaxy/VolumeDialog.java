package com.example.guardians_of_galaxy;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

public class VolumeDialog extends Dialog {

    public Button settingBtn;
    public SeekBar musicBar, effectBar;
    static public int musicVolumeValue = 3,effectVolumeValue = 3;

    public VolumeDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.volume_dialog);

        settingBtn = (Button) findViewById(R.id.settingBtn);
        musicBar = (SeekBar) findViewById(R.id.musicBar);
        effectBar = (SeekBar) findViewById(R.id.effectBar);
        musicBar.setProgress(musicVolumeValue);
        effectBar.setProgress(effectVolumeValue);

        musicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                musicVolumeValue = i;
                DisplayChange.mp.setVolume(i*0.1f, i*0.1f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        effectBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                effectVolumeValue = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
}
