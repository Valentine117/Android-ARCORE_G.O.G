package com.example.guardians_of_galaxy;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class GunDialog extends Dialog {

    Weapon mWeapon;

    public Button buyBtn, cancelBtn;
    public TextView gunName, gunDmg, maxBullets, shotSpeed, reloadingSpeed;
    public ImageView gunImg;
    int myGold, gunGold;

    public GunDialog(@NonNull Context context, int gunNum, int myGold) {
        super(context);
        setContentView(R.layout.gun_dialog);

        mWeapon = new Weapon(gunNum);

        this.myGold = myGold;
        buyBtn = (Button) findViewById(R.id.buyBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        gunName = (TextView) findViewById(R.id.gunName);
        gunImg = (ImageView) findViewById(R.id.gunImg);
        gunDmg = (TextView) findViewById(R.id.gunDmg);
        maxBullets = (TextView) findViewById(R.id.maxBullets);
        shotSpeed = (TextView) findViewById(R.id.shotSpeed);
        reloadingSpeed = (TextView) findViewById(R.id.reloadingSpeed);


        String imageName = "picture";

        gunDmg.setText("데미지 : " + mWeapon.dmg);
        maxBullets.setText("탄창 수 : " + mWeapon.bulletsMax);
        shotSpeed.setText("연사속도 : " + (12-mWeapon.actionCnt)+"");
        reloadingSpeed.setText("재장전 시간 : " + (double) mWeapon.reloadTime/1000 + "초");

        switch (gunNum) {
            case 1:
                gunName.setText("베레타");
                gunImg.setImageResource(R.drawable.gun1);
                gunGold = 100;
                break;
            case 2:
                gunName.setText("리볼버");
                gunImg.setImageResource(R.drawable.gun2);
                gunGold = 200;
                break;
            case 3:
                gunName.setText("P90");
                gunImg.setImageResource(R.drawable.gun3);
                gunGold = 300;
                break;
            case 4:
                gunName.setText("M4A1");
                gunImg.setImageResource(R.drawable.gun4);
                gunGold = 400;
                break;
            case 5:
                gunName.setText("ShotGun");
                gunImg.setImageResource(R.drawable.gun5);
                gunGold = 500;
                break;
            case 6:
                gunName.setText("HeavyMachineGun");
                gunImg.setImageResource(R.drawable.gun6);
                gunGold = 1000;
                break;
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
