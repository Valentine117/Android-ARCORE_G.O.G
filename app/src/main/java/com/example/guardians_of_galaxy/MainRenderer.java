package com.example.guardians_of_galaxy;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.ar.core.Session;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    interface RenderCallBack {
        void preRender();
    }

    CameraPreView mCamera;
    PlaneRenderer mPlane;
    RenderCallBack myCallBack;
    Context context;

    ObjRenderer mWeapon, mBullet, mGameOver;
    ArrayList<Monster> mMonsters;

    int width, height, monsterNum = 20, monsterStart = 15;  // 몬스터 최대숫자, 시작숫자
    boolean viewportChange = false;
    boolean isBegun = false, gameOver;

    MainRenderer(RenderCallBack myCallBack, Context context, Weapon weapon) {

        this.context = context;
        this.myCallBack = myCallBack;
        mCamera = new CameraPreView();
        mPlane = new PlaneRenderer(Color.WHITE, 0.7f);

        mWeapon = new ObjRenderer(context, weapon.objFileName, weapon.objTextureFileName);
        mBullet = new ObjRenderer(context, "andy.obj", "andy.png");
        mGameOver = new ObjRenderer(context, "gameover.obj", "andy.png");

        mMonsters = new ArrayList<>();
        for(int i = 0; i<monsterNum; i++){
            mMonsters.add(new Monster(context, (i%6+1)));
            if(i<monsterStart) {
                mMonsters.get(i).isLive = true;
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClearColor(0f, 1f, 1f, 1f);

        mCamera.init();
        mPlane.init();
        mWeapon.init();
        mBullet.init();
        mGameOver.init();
        
        for(int i=0 ; i<monsterNum ; i++) {
            mMonsters.get(i).mObj.init();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        viewportChange = true;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        myCallBack.preRender();
        GLES30.glDepthMask(false);

        mCamera.draw();
        GLES30.glDepthMask(true);

        if(!isBegun) {
            mPlane.draw();
        }
        mWeapon.draw();
        mBullet.draw();
        if(gameOver) {
            mGameOver.draw();
        }
        for(int i=0 ; i<monsterNum ; i++) {
            if(mMonsters.get(i).isLive) {
                mMonsters.get(i).mObj.draw();
            }
        }
    }

    void onDisplayChanged() {
        viewportChange = true;
    }

    int getTextureID() {    // 카메라의 색칠하기 id를 리턴한다.
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }

    void updateSession(Session session, int rotation) {
        if(viewportChange) {
            session.setDisplayGeometry(rotation, width, height);
            viewportChange = false;
        }
    }

    void updateProjMatrix(float [] matrix) {
        mPlane.setProjectionMatrix(matrix);
        mWeapon.setProjectionMatrix(matrix);
        mBullet.setProjectionMatrix(matrix);
        mGameOver.setProjectionMatrix(matrix);
        for(Monster andy : mMonsters){
            andy.mObj.setProjectionMatrix(matrix);
        }
    }

    void updateViewMatrix(float [] matrix) {
        mPlane.setViewMatrix(matrix);
        mWeapon.setViewMatrix(matrix);
        mBullet.setViewMatrix(matrix);
        mGameOver.setViewMatrix(matrix);
        for(Monster andy : mMonsters){
            andy.mObj.setViewMatrix(matrix);
        }
    }
}
