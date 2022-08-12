package com.example.guardians_of_galaxy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private View decorView;
    private int uiOption;
    private long backBtnTime = 0;

    MyDB myDB;
    SQLiteDatabase db;

    TextView bestScore;
    Button startBtn, storeBtn, manualBtn;
    ImageButton volumeBtn;

    class MyDB extends SQLiteOpenHelper {

        public MyDB(@Nullable Context context){
            super(context, "user.db",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Cursor cs =  db.rawQuery("select * from sqlite_master;", null);
            if(cs.getCount() == 1) {
                db.execSQL("CREATE TABLE userinfo(id INTEGER, weapon INTEGER, gold INTEGER, score INTEGER, " +
                        "gun1able INTEGER, gun2able INTEGER, gun3able INTEGER, gun4able INTEGER, gun5able INTEGER, gun6able INTEGER);");
                db.execSQL("insert into userinfo (id,weapon,gold,score, gun1able, gun2able, gun3able, " +
                        "gun4able, gun5able, gun6able) values (0,1,10000,0, 1,0,0,0,0,0);");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        decorView = getWindow().getDecorView();
        uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOption);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(getApplicationContext(), DisplayChange.class));

        myDB = new MyDB(this);
        db = myDB.getWritableDatabase();
        myDB.onCreate(db);

        Cursor cursor = db.rawQuery("select * from userinfo", null);
        cursor.moveToNext();

        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.finish();
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                startActivity(intent);
            }
        });

        storeBtn = (Button) findViewById(R.id.storeBtn);
        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.finish();
                Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                startActivity(intent);
            }
        });

        manualBtn = (Button) findViewById(R.id.manualBtn);
        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), ManualPage.class);
                startActivity(intent);
            }
        });

        volumeBtn = (ImageButton) findViewById(R.id.volumeBtn);
        volumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VolumeDialog vd = new VolumeDialog(MainActivity.this);
                vd.show();
            }
        });

        bestScore = (TextView)findViewById(R.id.bestScore);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bestScore.setText(cursor.getInt(3)+"");
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    @Override
    protected void onPause() {
        db.close();
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        stopService(new Intent(getApplicationContext(), DisplayChange.class));
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime){
            ActivityCompat.finishAffinity(this);
            System.exit(0);
            super.onBackPressed();
            stopService(new Intent(getApplicationContext(), DisplayChange.class));
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "뒤로가기 버튼 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

}