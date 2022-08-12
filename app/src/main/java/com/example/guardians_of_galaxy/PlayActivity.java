package com.example.guardians_of_galaxy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.display.DisplayManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PlayActivity extends Activity {

    private View decorView;
    private int uiOption;
    private SoundPool sp;
    private int s1, s2, s3;
//    MediaPlayer gunShotSound1, gunShotSound2, reLoadSound;

    Vibrator vibrator;
    Intent music;
    Session mSession;
    GLSurfaceView mySurfaceView;

    MainRenderer mRenderer;
    Weapon mWeapon;
    MyDB myDB;
    SQLiteDatabase db;
    Cursor cursor;

    Button btnBack;
    ImageView btnShot, btnReLoad;
    TextView remainMonsters, remainMonsters2, remainBullets, remainBullets2, reLoadingView,
            targetView, targetView2, playTimeView, playTimeView2, scoreView, scoreView2;
    ProgressBar reLoadBar;

    float [] weaponMatrix = new float[16];
    float [] bulletMatrix = new float[16];
    float [] gameOverMatrix = new float[16];

    ArrayList<float[]> modelMatrixAL;
    float[][] moveXYZs = new float[20][3];
    float[][] nowXYZs = new float[20][3];

    HashMap<Monster, Integer> monsterMovePeriod;

    float displayX, displayY;
    boolean mTouched = false, shooting = false, shooting2 = false, regen = true, firstSet = false;

    int monCnt = 0, score = 0;
    int weaponNum;
    int monsterMoveFrameCount = 100;
    int progress = 0;
    int startTime = 0, playTime = 0;

    class MyDB extends SQLiteOpenHelper {

        public MyDB(@Nullable Context context){
            super(context, "user.db",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // 네비게이션바 없애기
        decorView = getWindow().getDecorView();
        uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        uiOption |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOption);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        mySurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceview);

        music = new Intent(this, DisplayChange.class);
        startService(music);

//        gunShotSound1 = MediaPlayer.create(this, R.raw.singleshot);
//        gunShotSound2 = MediaPlayer.create(this, R.raw.shotgun);
//        reLoadSound = MediaPlayer.create(this, R.raw.gunreload);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            sp = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build();
        } else {
            sp = new SoundPool(6, AudioManager.STREAM_RING,0);
        }
        s1 = sp.load(this, R.raw.singleshot,1);
        s2 = sp.load(this, R.raw.shotgun,1);
        s3 = sp.load(this, R.raw.gunreload, 1);

        myDB = new MyDB(this);
        db = myDB.getWritableDatabase();
        cursor = db.rawQuery("select * from userinfo", null);
        cursor.moveToNext();
        weaponNum = cursor.getInt(1);
        mWeapon = new Weapon(weaponNum);

        btnShot = (ImageButton)findViewById(R.id.btnShot);
        btnReLoad = (ImageButton)findViewById(R.id.btnReLoad);
        btnBack = (Button)findViewById(R.id.btnBack);
        remainMonsters = (TextView)findViewById(R.id.remainMonsters);
        remainMonsters2 = (TextView)findViewById(R.id.remainMonsters2);
        remainBullets = (TextView)findViewById(R.id.bulletView);
        remainBullets2 = (TextView)findViewById(R.id.bulletView2);
        reLoadingView = (TextView)findViewById(R.id.reLoadingView);
        targetView = (TextView)findViewById(R.id.targetView);
        targetView2 = (TextView)findViewById(R.id.targetView2);
        playTimeView = (TextView)findViewById(R.id.playTimeView);
        playTimeView2 = (TextView)findViewById(R.id.playTimeView2);
        scoreView = (TextView)findViewById(R.id.scoreView);
        scoreView2 = (TextView)findViewById(R.id.scoreView2);
        reLoadBar = (ProgressBar)findViewById(R.id.reLoadBar);

        monsterMovePeriod = new HashMap<>();
        modelMatrixAL = new ArrayList<>();
        for(int i = 0; i<20; i++){  // 최대 몬스터 숫자
            modelMatrixAL.add(new float[16]);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remainBullets.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
                remainBullets2.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
            }
        });

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        if(displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }
            }, null);
        }

        MainRenderer.RenderCallBack mr = new MainRenderer.RenderCallBack() {

            @Override
            public void preRender() {
                if(mRenderer.viewportChange) {
                    Display display = getWindowManager().getDefaultDisplay();
                    mRenderer.updateSession(mSession, display.getRotation());
                }
                mSession.setCameraTextureName(mRenderer.getTextureID());

                Frame frame = null;
                try {
                    frame = mSession.update();  // 카메라의 화면을 업데이트한다.
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                mRenderer.mCamera.transformDisplayGeometry(frame);

                LightEstimate estimate = frame.getLightEstimate();
                float lightyIntensity = estimate.getPixelIntensity();

                if(mTouched && !mRenderer.isBegun) {
                    List<HitResult> results = frame.hitTest(displayX, displayY);
                    for (HitResult hr : results) {
                        Pose pose = hr.getHitPose();
                        Trackable trackable = hr.getTrackable();

                        if(trackable instanceof Plane && ((Plane)trackable).isPoseInPolygon(pose)) {
                            for(int i=0; i<mRenderer.monsterNum; i++){
                                pose.toMatrix(modelMatrixAL.get(i), 0);
                                pose.toMatrix(gameOverMatrix, 0);
                                Matrix.translateM(gameOverMatrix, 0, 0f, 0.4f, -0.25f);
                                Matrix.scaleM(gameOverMatrix, 0, 1.6f, 0.8f, 3f);
                                Matrix.scaleM(modelMatrixAL.get(i), 0, 1.5f, 1.5f, 1.5f);
                                Matrix.translateM(modelMatrixAL.get(i), 0, 0f, 0f, -3f);    // 몬스터 생성위치 3f 뒤쪽
                                monCnt = mRenderer.monsterStart;
                            }
                            startTime = (int) System.currentTimeMillis()/1000;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    targetView.setText("┼");
                                    targetView2.setText("◎");
                                    if(mWeapon.num==5) {
                                        targetView2.setTextSize(80f);
                                    }

                                    btnShot.setVisibility(View.VISIBLE);
                                    btnReLoad.setVisibility(View.VISIBLE);
                                    remainBullets.setVisibility(View.VISIBLE);
                                    remainBullets2.setVisibility(View.VISIBLE);
                                }
                            });
                            mRenderer.isBegun = true;
                        }
                    }
                    mTouched = false;
                }

                if(!mRenderer.gameOver) {
                    playTime = (int) System.currentTimeMillis() / 1000 - startTime;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (startTime != 0) {
                                playTimeView.setText("Time : " + playTime + "s");
                                playTimeView2.setText("Time : " + playTime + "s");
                            }
                        }
                    });
                }

                // 몬스터 생성
                for(int i=0 ; i<mRenderer.monsterNum ; i++) {
                    mRenderer.mMonsters.get(i).mObj.setLightIntensity(lightyIntensity);
                    mRenderer.mMonsters.get(i).mObj.setModelMatrix(modelMatrixAL.get(i));
                }

                // 몬스터 이동
                for(int x=0 ; x<mRenderer.monsterNum ; x++) {
                    if(monsterMoveFrameCount %monsterMovePeriod.get(mRenderer.mMonsters.get(x)) == 0) {
                        float monSpeed = mRenderer.mMonsters.get(x).speed;
                        moveXYZs[x][0] = (float) (Math.random() * 0.05f - 0.025f) * monSpeed;
                        moveXYZs[x][1] = (float) (Math.random() * 0.05f - 0.025f) * monSpeed;
                        moveXYZs[x][2] = (float) (Math.random() * 0.05f - 0.025f) * monSpeed;
                    }
                    if(monsterMoveFrameCount == Integer.MAX_VALUE) monsterMoveFrameCount = 0;
                }
                monsterMoveFrameCount++;

                for(int i=0 ; i<mRenderer.monsterNum ; i++){
                    moveXYZs[i][0] = (nowXYZs[i][0] + moveXYZs[i][0]) > -5  && (nowXYZs[i][0] + moveXYZs[i][0]) < 5 ? moveXYZs[i][0] : - moveXYZs[i][0];
                    moveXYZs[i][1] = (nowXYZs[i][1] + moveXYZs[i][1]) > 0  && (nowXYZs[i][1] + moveXYZs[i][1]) < 2 ? moveXYZs[i][1] : - moveXYZs[i][1];
                    moveXYZs[i][2] = (nowXYZs[i][2] + moveXYZs[i][2]) > 1  && (nowXYZs[i][2] + moveXYZs[i][2]) < 5 ? moveXYZs[i][2] : - moveXYZs[i][2];

                    nowXYZs[i][0] += moveXYZs[i][0];
                    nowXYZs[i][1] += moveXYZs[i][1];
                    nowXYZs[i][2] += moveXYZs[i][2];

                    Matrix.translateM(modelMatrixAL.get(i), 0, moveXYZs[i][0], moveXYZs[i][1], moveXYZs[i][2]);
                }

                // 평면찾기
                if(!mRenderer.isBegun) {
                    Collection<Plane> planes = mSession.getAllTrackables(Plane.class);
                    for (Plane plane : planes) {
                        if (plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null) {
                            mRenderer.mPlane.update(plane);
                        }
                    }
                }

                Camera camera = frame.getCamera();

                float [] projMatrix = new float[16];
                float [] projMatrix2 = new float[16];
                float [] viewMatrix = new float[16];

                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
                camera.getProjectionMatrix(projMatrix2, 0, 100f, 200f);
                camera.getViewMatrix(viewMatrix, 0);

                float [] mViewPoint = getScreenPoint(mRenderer.width/2, mRenderer.height/2,
                        mRenderer.width, mRenderer.height, projMatrix, viewMatrix);
                float [] mViewPoint2 = getScreenPoint(mRenderer.width/2, mRenderer.height/2,
                        mRenderer.width, mRenderer.height, projMatrix2, viewMatrix);

                float dx = mViewPoint[0] - mViewPoint2[0];
                float dy = mViewPoint[1] - mViewPoint2[1];
                float dz = mViewPoint[2] - mViewPoint2[2];

                // 화면전환에 따른 회전 각도
                float degHorizon = (float)Math.atan2(dx, dz) * 180 / (float)Math.PI;
                float degVertical = (float)Math.atan2(dy, Math.abs(dz)) * 180 / (float)Math.PI;

                float [] mGunInitPos = calculateInitialGunPoint(mRenderer.width,
                        mRenderer.height, projMatrix, viewMatrix);    // 총위치

                // 무기
                if(!shooting2) {
                    weaponMatrix = new float[16];
                    Matrix.setIdentityM(weaponMatrix, 0);
                    Matrix.translateM(weaponMatrix, 0, mGunInitPos[0], mGunInitPos[1], mGunInitPos[2]);
                    Matrix.rotateM(weaponMatrix, 0, 80f, 0f, -1f, 0f);
                    Matrix.rotateM(weaponMatrix, 0, degHorizon, 0f, 1f, 0f);
                    Matrix.rotateM(weaponMatrix, 0, degVertical * 0.6f, 0f, 0f, 1f);
                    Matrix.scaleM(weaponMatrix, 0, 0.7f, 0.7f, 0.7f);
                    Matrix.translateM(weaponMatrix, 0, 0f, 0f, -0.08f);
                    mRenderer.mWeapon.setLightIntensity(lightyIntensity);
                    mRenderer.mWeapon.setModelMatrix(weaponMatrix);
                }

                // 명중 판별용 총알
                if(!shooting) {
                    bulletMatrix = new float[16];
                    Matrix.setIdentityM(bulletMatrix, 0);
                    Matrix.translateM(bulletMatrix, 0, mViewPoint[0], mViewPoint[1], mViewPoint[2]);
                    Matrix.scaleM(bulletMatrix, 0, 0.001f, 0.001f, 0.001f);
                    Matrix.rotateM(bulletMatrix, 0, degHorizon, 0f, 1f, 0f);
                    Matrix.rotateM(bulletMatrix, 0, -degVertical, 1f, 0f, 0f);
                    Matrix.rotateM(bulletMatrix, 0, -90f, 1f, 0f, 0f);
                }

                mRenderer.mGameOver.setLightIntensity(lightyIntensity);
                mRenderer.mGameOver.setColorCorrection(new float[] {0.8f, 0f, 0f, 0.7f});
                mRenderer.mGameOver.setModelMatrix(gameOverMatrix);

                mRenderer.updateProjMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);

                // 몬스터 리젠
                if(playTime % 5==0 && !regen) {

                    ArrayList<Integer> minimalLV= new ArrayList<>();

                    for (int i = 0; i<mRenderer.monsterNum; i++) {
                        if(!mRenderer.mMonsters.get(i).isLive) minimalLV.add(mRenderer.mMonsters.get(i).lv);
                    }

                    if(!minimalLV.isEmpty()) {
                        minimalLV.sort(new Comparator<Integer>() {
                            @Override
                            public int compare(Integer t1, Integer t2) {
                                return t1.compareTo(t2);
                            }
                        });
                    } else {
                        minimalLV.add(0);
                        gameOver();
                    }

                    for(int i=0 ; i<mRenderer.mMonsters.size() ; i++) {
                        if(!mRenderer.mMonsters.get(i).isLive && mRenderer.mMonsters.get(i).lv == minimalLV.get(0)) {
                            float monScale = mRenderer.mMonsters.get(i).scale;
                            Matrix.scaleM(modelMatrixAL.get(i), 0, monScale, monScale, monScale);
                            mRenderer.mMonsters.get(i).isLive = true;
                            monCnt ++;
                            break;
                        }
                    }
                    regen = true;
                }

                if(playTime%5 == 3 && regen) {
                    regen = false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remainMonsters.setText("남은 몬스터 수 : " + monCnt);
                        remainMonsters2.setText("남은 몬스터 수 : " + monCnt);
                    }
                });
            }
        };

        mRenderer = new MainRenderer(mr, this, mWeapon);

        if(!firstSet) {
            for (int i = 0; i < mRenderer.mMonsters.size(); i++) {
                monsterMovePeriod.put(mRenderer.mMonsters.get(i), 50 + (int) Math.random() * 30 - 15);
            }
            firstSet = true;
        }

        mySurfaceView.setPreserveEGLContextOnPause(true);
        mySurfaceView.setEGLContextClientVersion(3);
        mySurfaceView.setRenderer(mRenderer);
        mySurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        btnReLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnShot.setEnabled(false);
                btnReLoad.setEnabled(false);
                targetView.setVisibility(View.INVISIBLE);
                targetView2.setVisibility(View.INVISIBLE);
                reLoadBar.setVisibility(View.VISIBLE);
                reLoadingView.setVisibility(View.VISIBLE);
                sp.play(s3, (float)VolumeDialog.effectVolumeValue*0.05f,
                        (float)VolumeDialog.effectVolumeValue*0.05f, 0, 0, 1f);

                Handler handler = new Handler();
                Handler handler2 = new Handler();

                Runnable r = new Runnable() {
                    public void run() {
                        mWeapon.reload();
                        btnShot.setEnabled(true);
                        btnReLoad.setEnabled(true);
                        reLoadBar.setVisibility(View.INVISIBLE);
                        reLoadingView.setVisibility(View.INVISIBLE);
                        targetView.setVisibility(View.VISIBLE);
                        targetView2.setVisibility(View.VISIBLE);
                        targetView.setText("┼");
                        targetView2.setText("◎");
                        if(mWeapon.num==5) {
                            targetView2.setTextSize(80f);
                        }
                        remainBullets.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
                        remainBullets2.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
                    }
                };

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0 ; i<100 ; i++) {
                            progress = i;
                            handler2.post(new Runnable() {
                                @Override
                                public void run() {
                                    reLoadBar.setProgress(progress);
                                }
                            });
                            try {
                                Thread.sleep(mWeapon.reloadTime/100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                handler.postDelayed(r, mWeapon.reloadTime);
                thread.start();
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        requestPermission();

        if(mSession == null) {
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, true)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Config config = new Config(mSession);
                        config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
                        mSession.configure(config);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mySurfaceView.onResume();
    }

    @Override
    protected void onPause() {

        if(cursor.getInt(3) < score) {
            db.execSQL("update userinfo set score = "+ score +" where id = 0;");
        }
        db.execSQL("update userinfo set gold = "+ (cursor.getInt(2)+score) +" where id = 0;");
        db.close();

        super.onPause();
        mySurfaceView.onPause();
        mSession.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        displayX = event.getX();
        displayY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            mTouched = true;
        }
        return true;
    }

    void requestPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String [] { Manifest.permission.CAMERA },
                    0
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            decorView.setSystemUiVisibility(uiOption);
        }
    }

    public void shot(View view) {

        if(mWeapon.remainBullets == 0) {  // 총알이 없을시
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    targetView.setText("Reload!!!");
                    targetView2.setText("");
                }
            });
        } else {

            shooting = true;
            vibrator.vibrate(100);
            if(mWeapon.num == 5) {  // 총성
                sp.play(s2, (float)VolumeDialog.effectVolumeValue*0.02f,
                        (float)VolumeDialog.effectVolumeValue*0.02f, 0, 0, 1f);
            } else {
                MediaPlayer gunShotSound1 = MediaPlayer.create(this, R.raw.singleshot);
                gunShotSound1.start();
//                if(gunShotSound1.isPlaying()) {
//                    gunShotSound1.reset();
//                    gunShotSound1 = MediaPlayer.create(this, R.raw.singleshot);
//                }
//                Log.d("총소리 여", gunShotSound1.isPlaying()+"");
//                gunShotSound1.start();
//                sp.play(s1, (float)VolumeDialog.effectVolumeValue*0.02f,
//                        (float)VolumeDialog.effectVolumeValue*0.02f, 0, 0, 1f);
            }
            mWeapon.shoot();
            shootingAction();

            for (int i = 0; i < mRenderer.monsterNum; i++) {

                double distance = bulletAndyDistance(mRenderer.mBullet, bulletMatrix, mRenderer.mMonsters.get(i).mObj);

                if (mRenderer.mMonsters.get(i).isLive && distance < mWeapon.aimRange*mRenderer.mMonsters.get(i).size) {

                    vibrator.vibrate(500);

                    if (mRenderer.mMonsters.get(i).hp <= mWeapon.dmg) {
                        mRenderer.mMonsters.get(i).isLive = false;
                        score += mRenderer.mMonsters.get(i).score;
                        monCnt--;
                        mRenderer.mMonsters.get(i).levelUp();
                    } else {
                        mRenderer.mMonsters.get(i).hp -= mWeapon.dmg;
                        if (mRenderer.mMonsters.get(i).hp < 7) {
                            mRenderer.mMonsters.get(i).mObj.setColorCorrection(
                                    mRenderer.mMonsters.get(i).lvColor[mRenderer.mMonsters.get(i).hp - 1]);
                        }
                    }

                    if (mWeapon.num != 5) {
                        break;
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remainMonsters.setText("남은 몬스터 수 : " + monCnt);
                    remainMonsters2.setText("남은 몬스터 수 : " + monCnt);
                    scoreView.setText("Score : " + score);
                    scoreView2.setText("Score : " + score);
                    remainBullets.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
                    remainBullets2.setText(mWeapon.remainBullets + "|" + mWeapon.bulletsMax);
                }
            });
            shooting = false;
        }
    }

    public void shootingAction() {
        shooting2 = true;
        btnShot.setEnabled(false);

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                btnShot.setEnabled(true);
            }
        };
        handler.postDelayed(r, mWeapon.actionCnt*30);

        for(int i=0 ; i<mWeapon.actionCnt ; i++) {
            Matrix.translateM(weaponMatrix, 0, mWeapon.actionMoving, 0f, 0f);
            Matrix.rotateM(weaponMatrix, 0, -mWeapon.actionAngle, 0f, 0f, 1f);
            mRenderer.mWeapon.setModelMatrix(weaponMatrix);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        shooting2 = false;
    }

    public void gameOver() {
        mRenderer.gameOver = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnShot.setEnabled(false);
                btnReLoad.setEnabled(false);
                btnBack.setVisibility(View.VISIBLE);
            }
        });
    }

    // 셀카봉모드
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mRenderer.isBegun) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (mWeapon.remainBullets == 0 && btnReLoad.isEnabled()) {
                        btnReLoad.performClick();
                    } else if(!shooting && btnShot.isEnabled()) {
                        shot(btnShot);
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onDestroy(){
        super.onDestroy();
        sp.release();
        sp = null;
    }

    public void backMain(View view) {
        this.finish();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        ExitDialog ed = new ExitDialog(this, PlayActivity.this);
        ed.show();
    }


    // 계산식 ----------
    // 총 최초 위치 설정
    public float[] calculateInitialGunPoint(int width, int height,
                                            float[] projMat, float[] viewMat) {
        return getScreenPoint(width / 2, height*0.8f, width, height, projMat, viewMat);
    }

    // 평면화
    public float[] getScreenPoint(float x, float y, float w, float h,
                                  float[] projMat, float[] viewMat) {
        float[] position = new float[3];
        float[] direction = new float[3];

        x = x * 2 / w - 1.0f;
        y = (h - y) * 2 / h - 1.0f;

        float[] viewProjMat = new float[16];
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0);

        float[] invertedMat = new float[16];
        Matrix.setIdentityM(invertedMat, 0);
        Matrix.invertM(invertedMat, 0, viewProjMat, 0);

        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];

        Matrix.multiplyMV(nearPlanePoint, 0, invertedMat, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedMat, 0, farScreenPoint, 0);

        position[0] = nearPlanePoint[0] / nearPlanePoint[3];
        position[1] = nearPlanePoint[1] / nearPlanePoint[3];
        position[2] = nearPlanePoint[2] / nearPlanePoint[3];

        direction[0] = farPlanePoint[0] / farPlanePoint[3] - position[0];
        direction[1] = farPlanePoint[1] / farPlanePoint[3] - position[1];
        direction[2] = farPlanePoint[2] / farPlanePoint[3] - position[2];

        normalize(direction);

        position[0] += (direction[0] * 0.1f);
        position[1] += (direction[1] * 0.1f);
        position[2] += (direction[2] * 0.1f);

        return position;
    }

    public void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    // 점과 직선사이 거리
    public double bulletAndyDistance(ObjRenderer bullet, float[] bulletMatrix, ObjRenderer andy) {

        double distance = 0;
        float [] andyPos = andy.getPoint();

        float [] a = {0, 0, 0};
        float [] b = {0, 0, 0};

        for(int i=0 ; i<3 ; i++) {
            Matrix.translateM(bulletMatrix, 0, 0f, 10f, 0f);
            float [] bulPos = bullet.getPoint();
            bullet.setModelMatrix(bulletMatrix);
            if(i==1) {
                a[0] = bulPos[0];
                a[1] = bulPos[1];
                a[2] = bulPos[2];
            } else if(i==2) {
                b[0] = bulPos[0] - a[0];
                b[1] = bulPos[1] - a[1];
                b[2] = bulPos[2] - a[2];
            }
        }
        float [] c = {andyPos[0]-a[0], andyPos[1]-a[1], andyPos[2]-a[2]};
        float [] bc = {b[1]*c[2]-b[2]*c[1], b[2]*c[0]-b[0]*c[2], b[0]*c[1]-b[1]*c[0]};

        double abDis = Math.pow(b[0]*b[0]+b[1]*b[1]+b[2]*b[2], 0.5); // 2만 나옴
        double acDis = Math.pow(bc[0]*bc[0]+bc[1]*bc[1]+bc[2]*bc[2], 0.5);

        distance = acDis/abDis;

        return distance;
    }

}