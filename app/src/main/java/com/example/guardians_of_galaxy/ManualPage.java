package com.example.guardians_of_galaxy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ManualPage extends AppCompatActivity {

    private View decorView;
    private int uiOption;

    Button backBtn;

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
        setContentView(R.layout.activity_manual);

        backBtn  = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
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
    public void onBackPressed() {
        this.finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}