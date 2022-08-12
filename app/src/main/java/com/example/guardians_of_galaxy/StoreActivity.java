package com.example.guardians_of_galaxy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class StoreActivity extends AppCompatActivity {

    private View decorView;
    private int uiOption;
    private GunDialog gunDialog;

    MyDB myDB;
    SQLiteDatabase db;
    Cursor cursor;
    Weapon temp;

    Button buyBtn1,buyBtn2,buyBtn3,buyBtn4,buyBtn5,buyBtn6, btnBack;
    TextView goldText;
    int myGold;
    HashMap<Integer, Integer> gunValue;

    class MyDB extends SQLiteOpenHelper {

        public MyDB(@Nullable Context context){
            super(context, "user.db",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        decorView = getWindow().getDecorView();

        uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(uiOption);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        myDB = new MyDB(this);
        db = myDB.getWritableDatabase();
        cursor = db.rawQuery("select * from userinfo", null);
        cursor.moveToNext();
        myGold = cursor.getInt(2);

        gunValue = new HashMap<Integer, Integer>();

        buyBtn1 = (Button) findViewById(R.id.buyBtn1);
        buyBtn2 = (Button) findViewById(R.id.buyBtn2);
        buyBtn3 = (Button) findViewById(R.id.buyBtn3);
        buyBtn4 = (Button) findViewById(R.id.buyBtn4);
        buyBtn5 = (Button) findViewById(R.id.buyBtn5);
        buyBtn6 = (Button) findViewById(R.id.buyBtn6);

        btnBack = (Button) findViewById(R.id.btnBack);
        goldText = (TextView)findViewById(R.id.gold);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                goldText.setText(myGold + "G");
            }
        });

        buyBtn1.setOnClickListener(new ForGunButton(1));
        buyBtn2.setOnClickListener(new ForGunButton(2));
        buyBtn3.setOnClickListener(new ForGunButton(3));
        buyBtn4.setOnClickListener(new ForGunButton(4));
        buyBtn5.setOnClickListener(new ForGunButton(5));
        buyBtn6.setOnClickListener(new ForGunButton(6));

        db.close();
    }

    class ForGunButton implements View.OnClickListener{

        int gunNum;
        public ForGunButton(int gunNum) {
            this.gunNum = gunNum;
        }

        @Override
        public void onClick(View v) {

            myDB = new MyDB(StoreActivity.this);
            db = myDB.getWritableDatabase();
            temp = new Weapon(gunNum);

            cursor = db.rawQuery("select * from userinfo", null);
            cursor.moveToNext();

            gunDialog = new GunDialog(StoreActivity.this, gunNum, myGold);
            gunDialog.show();

            gunDialog.buyBtn.setOnClickListener(new ForGunBuyButtonOnDialog(gunNum));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(cursor.getInt(gunNum+3) == 0) {
                        gunValue.put(gunNum, temp.value);
                    } else {
                        gunValue.put(gunNum, 0);
                        gunDialog.buyBtn.setText("장착");
                        gunDialog.buyBtn.setEnabled(true);
                    }

                    if(cursor.getInt(gunNum+3) == 0){
                        gunDialog.buyBtn.setText(temp.value + "G" + "\n구매");
                    }

                    if((myGold<gunValue.get(gunNum) && cursor.getInt(gunNum+3) == 0)){
                        gunDialog.buyBtn.setEnabled(false);
                    }

                    if(cursor.getInt(1) == gunNum){
                        gunDialog.buyBtn.setText("장착 중");
                        gunDialog.buyBtn.setEnabled(false);
                    }
                }
            });

            db.close();
        }
    }

    class ForGunBuyButtonOnDialog implements View.OnClickListener{

        int gunNum;

        public ForGunBuyButtonOnDialog(int gunNum) {
            this.gunNum = gunNum;
        }

        @Override
        public void onClick(View v) {

            myDB = new MyDB(StoreActivity.this);
            db = myDB.getWritableDatabase();
            cursor = db.rawQuery("select * from userinfo", null);
            cursor.moveToNext();

            myGold -= gunValue.get(gunNum);
            db.execSQL("update userinfo set gold = " + (myGold) + " where id = 0;");
            db.execSQL("update userinfo set weapon = " + gunNum + " where id = 0;");
            db.execSQL("update userinfo set gun"+gunNum+"able = 1 where id = 0;");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    goldText.setText(myGold + "G");
                    gunDialog.buyBtn.setEnabled(false);
                    gunDialog.buyBtn.setText("장착 중");
                }
            });

            db.close();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    public void backMain(View view) {
        this.finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}