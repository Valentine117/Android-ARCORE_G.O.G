package com.example.guardians_of_galaxy;

public class Weapon {

    int num, value, dmg, bulletsMax, remainBullets, reloadTime, actionCnt;
    float actionMoving, actionAngle;
    double aimRange;
    String name = "", objFileName = "", objTextureFileName = "";

    public Weapon(int num) {

        this.num = num;

        switch (num) {
            case 1:
                name = "Pistol";
                objFileName = "gun1.obj";
                objTextureFileName = "gun1.png";
                value = 100;
                dmg = 1;
                bulletsMax = 10;
                reloadTime = 800;
                aimRange = 0.2;
                actionCnt = 6;
                actionMoving = 0.01f;
                actionAngle = 4;
                break;

            case 2:
                name = "Revolver";
                objFileName = "gun2.obj";
                objTextureFileName = "gun2.png";
                value = 200;
                dmg = 3;
                bulletsMax = 6;
                reloadTime = 1200;
                aimRange = 0.2;
                actionCnt = 6;
                actionMoving = 0.02f;
                actionAngle = 7;
                break;

            case 3:
                name = "P90";
                objFileName = "gun3.obj";
                objTextureFileName = "gun3.png";
                value = 300;
                dmg = 1;
                bulletsMax = 30;
                reloadTime = 2500;
                aimRange = 0.2;
                actionCnt = 2;
                actionMoving = 0.01f;
                actionAngle = 4;
                break;

            case 4:
                name = "M4A1";
                objFileName = "gun4.obj";
                objTextureFileName = "gun4.png";
                value = 400;
                dmg = 2;
                bulletsMax = 20;
                reloadTime = 2500;
                aimRange = 0.2;
                actionCnt = 4;
                actionMoving = 0.03f;
                actionAngle = 1.5f;
                break;

            case 5:
                name = "5";
                objFileName = "gun5.obj";
                objTextureFileName = "gun5.png";
                value = 500;
                dmg = 1;
                bulletsMax = 2;
                reloadTime = 3000;
                aimRange = 0.4;
                actionCnt = 10;
                actionMoving = 0.02f;
                actionAngle = 5;
                break;

            case 6:
                name = "6";
                objFileName = "gun6.obj";
                objTextureFileName = "gun6.png";
                value = 20000;
                dmg = 3;
                bulletsMax = 60;
                reloadTime = 3000;
                aimRange = 0.2;
                actionCnt = 2;
                actionMoving = 0.01f;
                actionAngle = 5;
                break;
        }
        remainBullets = bulletsMax;
    }

    public void reload() {
        remainBullets = bulletsMax;
    }

    public void shoot() {
        if(remainBullets > 0) {
            remainBullets -= 1;
        }
    }

}