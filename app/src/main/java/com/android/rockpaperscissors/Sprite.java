package com.android.rockpaperscissors;

import static java.lang.Math.abs;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.concurrent.ThreadLocalRandom;

public class Sprite {

    private final int size, speed, screenWidth, bottomWall;
    private Bitmap image;
    private float x, y;
    private Sprite closestPrey, closestHunter;
    private Sprite[] ownGroup, hunterGroup, preyGroup;

    public Sprite(int size, Bitmap image, int x, int y, Sprite[] ownGroup, Sprite[] hunterGroup, Sprite[] preyGroup, int speed) {
        this.size = size;
        this.speed = speed;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        bottomWall = screenHeight - screenHeight /15;
        this.image = image;
        this.x = x;
        this.y = y;
        this.ownGroup = ownGroup;
        this.hunterGroup = hunterGroup;
        this.preyGroup = preyGroup;
    }

    public float[] getCoordinates() {
        return new float[]{x, y};
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setOwnGroup(Sprite[] ownGroup) {
        this.ownGroup = ownGroup;
    }

    public void setHunterGroup(Sprite[] hunterGroup) {
        this.hunterGroup = hunterGroup;
    }

    public void setPreyGroup(Sprite[] preyGroup) {
        this.preyGroup = preyGroup;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
    }

    public void update(){
        moveSprite();
        checkWalls();
        wallAvoidance();
        selfCollision();
        eat();
    }

    public void randomMovement() {
        double randomX = ThreadLocalRandom.current().nextDouble(speed*-1, speed);
        double randomY = ThreadLocalRandom.current().nextDouble(speed*-1, speed);
        x += randomX;
        y += randomY;
    }

    public void checkWalls() {
        // sprite touches left wall
        if (x <= 20) {
            x = 20;
        }
        // sprite touches right wall
        if (x + size >= screenWidth-20) {
            x = screenWidth-20-size;
        }
        // sprite touches top wall
        if (y <= 20) {
            y = 20;
        }
        // sprite touches bottom wall
        if (y + size >= bottomWall-20) {
            y = bottomWall-20-size;
        }
    }

    private void wallAvoidance(){
        // Only avoid walls in the outer quarters
        if (!(x+(size*0.5) < screenWidth*0.25 || x+(size*0.5) > screenWidth*0.75 ||
                y+(size*0.5) < bottomWall*0.25 || y+(size*0.5) > bottomWall*0.75)) {
            return;
        }
        double avoidanceWeight = 0.002;
        int dirX = 1;
        double avoidanceX = screenWidth * 0.5 - x + (size * 0.5);
        int dirY = 1;
        double avoidanceY = bottomWall * 0.5 - y + (size * 0.5);
        if (x + (size * 0.5) >= screenWidth * 0.5) {
            dirX = -1;
            avoidanceX = x + (size * 0.5) - screenWidth * 0.5;
        }
        if (y + (size * 0.5) >= bottomWall * 0.5) {
            dirY = -1;
            avoidanceY = y + (size * 0.5) - bottomWall * 0.5;
        }
        x += dirX * avoidanceX * avoidanceWeight;
        y += dirY * avoidanceY * avoidanceWeight;
    }

    public void moveSprite() {
        closestHunter = getInfo(hunterGroup);
        closestPrey = getInfo(preyGroup);
        randomMovement();
        hunt();
        run();
    }

    public void selfCollision() {
        for (Sprite sprite : ownGroup) {
            if (sprite == this || sprite == null) {
                continue;
            }
            float[] spriteCoordinates = sprite.getCoordinates();
            float distanceX = x - spriteCoordinates[0];
            float distanceY = y - spriteCoordinates[1];
            // if both distances are smaller then the Sprite size, then they are colliding
            if (!(abs(distanceY) <= size && abs(distanceX) <= size)) {
                continue;
            }
            // this on right
            if (distanceX > 0) {
                x += speed;
            }
            // this on left
            else if (distanceX < 0) {
                x -= speed;
            }
            // this on bottom
            if (distanceY > 0) {
                y += speed;
            }
            // this on top
            else if (distanceY < 0) {
                y -= speed;
            }
        }
    }

    public void hunt(){
        if(closestPrey != null){
            double dX = (closestPrey.x + (size*0.5)- x);
            double dY = (closestPrey.y + (size*0.5) - y);
            double distance = Math.sqrt(Math.pow(dY, 2) + Math.pow(dX, 2));
            x += (dX / distance) * speed;
            y += (dY / distance) * speed;
        }
    }

    public void run(){
        // Run if hunter exists and closer than 500
        if(closestHunter == null) {
            return;
        }
        double dX = (closestHunter.x + (size*0.5) - x);
        double dY = (closestHunter.y + (size*0.5) - y);
        double distance = Math.sqrt(Math.pow(dY, 2) + Math.pow(dX, 2));
        if(distance > 500) {
            return;
        }
        x -= (dX / distance) * speed * 0.95;
        y -= (dY / distance) * speed * 0.95;
    }

    public void eat(){
        for(int i=0; i<preyGroup.length; i++){
            Sprite sprite = preyGroup[i];
            if(sprite == null) {
                continue;
            }
            float[] spriteCoordinates = sprite.getCoordinates();
            double distanceX = x - spriteCoordinates[0];
            double distanceY = y - spriteCoordinates[1];
            double hitBox = size*0.7;
            if (!(abs(distanceY) <= hitBox && abs(distanceX) <= hitBox)) {
                continue;
            }
            preyGroup[i] = null;
            ownGroup[i] = sprite;
            sprite.setImage(image);
            sprite.setOwnGroup(ownGroup);
            sprite.setHunterGroup(hunterGroup);
            sprite.setPreyGroup(preyGroup);
        }
    }

    public Sprite getInfo(Sprite[] group){
        Sprite closestSprite = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for(Sprite sprite: group){
            if(sprite == this || sprite == null) {
                continue;
            }
            float[] spriteCoordinates = sprite.getCoordinates();
            double distance = Math.sqrt(Math.pow(
                    (spriteCoordinates[1] - y), 2) + Math.pow((spriteCoordinates[0] - x), 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSprite = sprite;
            }
        }
        return closestSprite;
    }
}
