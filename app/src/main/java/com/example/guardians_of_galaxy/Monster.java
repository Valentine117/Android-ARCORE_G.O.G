package com.example.guardians_of_galaxy;

import android.content.Context;

public class Monster {

    ObjRenderer mObj;
    Context context;

    public int lv, hp, maxHp, score;
    public float speed, scale, size;
    public String name, objFileName, objTextureFileName;
    float [][] lvColor;
    boolean isLive;

    Monster(Context context, int monsterTypeNumber) {

        this.context = context;
        isLive = false;
        lv = 1;
        maxHp = 1;
        hp = maxHp;
        speed = 1;
        scale = 1;
        score = 1;

        lvColor = new float[][]{{0.8f, 0.8f, 0.8f, 0.8f}, {1f, 0f, 0f, 0.6f}, {0f, 0f, 1f, 0.6f},
                {1f, 0f, 1f, 0.6f}, {1f, 1f, 0f, 0.6f}, {1f, 0f, 0f, 0.6f}};

        switch (monsterTypeNumber) {
            case 1:
                name = "mon1";
                objFileName = "mon1.obj";
                objTextureFileName = "mon1.png";
                size = 1.5f;
                break;
            case 2:
                name = "mon2";
                objFileName = "mon2.obj";
                objTextureFileName = "mon2.png";
                size = 2f;
                break;
            case 3:
                name = "mon3";
                objFileName = "mon3.obj";
                objTextureFileName = "mon3.png";
                size = 1.2f;
                break;
            case 4:
                name = "mon4";
                objFileName = "mon4.obj";
                objTextureFileName = "mon4.png";
                size = 2.5f;
                break;
            case 5:
                name = "mon8";
                objFileName = "mon8.obj";
                objTextureFileName = "mon8.png";
                size = 1.8f;
                break;
            default:
                name = "mon6";
                objFileName = "mon7.obj";
                objTextureFileName = "mon7.png";
                size = 1.5f;
                break;
        }

        mObj = new ObjRenderer(context, objFileName, objTextureFileName);
    }

    public void levelUp() {

        lv ++;
        maxHp ++;
        hp = maxHp;
        speed = speed * 1.2f;
        scale = scale * 0.9f;
        score += lv;

        mObj.setColorCorrection(lv<7 ? lvColor[lv-1] : lvColor[5]);
    }

}