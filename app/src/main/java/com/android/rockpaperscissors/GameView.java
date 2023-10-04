package com.android.rockpaperscissors;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final com.android.rockpaperscissors.MainThread thread;
    private final int screenWidth, screenHeight, bottomWall, groupSize, speed, spriteSize;
    private final Bitmap rockImage, paperImage, scissorsImage;
    private final Paint paint;
    private final ArrayList<Sprite> allSprites;
    private Sprite[] rocks, papers, scissors;

    public GameView(Context context,int groupSize, int speed) {
        super(context);
        getHolder().addCallback(this);
        thread = new com.android.rockpaperscissors.MainThread(getHolder(), this);
        setFocusable(true);

        // what to do when back button is pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
//                TODO: back button should pause the game
//                thread.setRunning(false);
//                Intent intent = new Intent(getContext(), MainActivity.class);
//                getContext().startActivity(intent);
            }
        };

        this.groupSize = groupSize;
        this.speed = speed;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        bottomWall = screenHeight - screenHeight /15;
        // Auto-sizing sprites based on groupSize. Not perfect but works OK.
        spriteSize = (int) Math.sqrt(((float)bottomWall*screenWidth)/(groupSize*3))/2;

        // create and scale bitmaps for sprite images
        rockImage = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.rock),
                spriteSize, spriteSize, true);
        paperImage = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.paper),
                spriteSize, spriteSize, true);
        scissorsImage = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.scissors),
                spriteSize, spriteSize, true);

        paint = new Paint();

        allSprites = new ArrayList<>();

        rocks = new Sprite[groupSize*3];
        papers = new Sprite[groupSize*3];
        scissors = new Sprite[groupSize*3];
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        // create sprites
        createSprites(groupSize, rockImage, 0, rocks, papers, scissors);
        createSprites(groupSize, paperImage, groupSize, papers, scissors, rocks);
        createSprites(groupSize, scissorsImage, 2*groupSize, scissors, rocks, papers);

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    /**
     * Create all sprites for a sprite group.
     * @param groupSize     number of sprites in group.
     * @param image         bitmap for sprite image.
     * @param offset        offset in sprite group, to fill Sprite[].
     * @param ownGroup      Sprite[] of the sprites.
     * @param hunterGroup   Sprite[] for hunters.
     * @param preyGroup     Sprite[] for prey.
     */
    private void createSprites(int groupSize, Bitmap image, int offset, Sprite[] ownGroup, Sprite[] hunterGroup, Sprite[] preyGroup) {
        for (int i = 0; i < groupSize; i++) {
            Random random = new Random();
            int randomX = random.nextInt(screenWidth - spriteSize);
            int randomY = random.nextInt(bottomWall - spriteSize);
            Sprite sprite = new Sprite(spriteSize, image, randomX, randomY, ownGroup, hunterGroup, preyGroup, speed);
            ownGroup[i + offset] = sprite;
            allSprites.add(sprite);
        }
    }

    /**
     * Checks if a sprite group has won.
     * Updates all sprites.
     */
    public void updateSprites(){
        checkWinner();
        for(Sprite sprite: allSprites) {
            sprite.update();
        }
    }

    /**
     * Counts all sprites in a group.
     * @param group Group to be counted.
     * @return      Returns int number of sprites in group.
     */
    private int countSprites(Sprite[] group){
        int count = 0;
        for(Sprite sprite : group)
            if (sprite != null) {
                ++count;
            }
        return count;
    }

    /**
     * Checks if a sprite group has won.
     * Uses countSprites() to count sprites in each group.
     * If a group has all the sprites (groupSize * 3), it is the winner.
     * In this case it stops the thread and goes to GameOver.java Activity.
     */
    public void checkWinner(){
        Sprite[][] spriteGroups = new Sprite[][]{rocks, papers, scissors};
        for(Sprite[] group : spriteGroups){
            if(countSprites(group) == groupSize*3) {
                String winner = "Rock";
                if (group == papers) {
                    winner = "Paper";
                }
                if (group == scissors) {
                    winner = "Scissors";
                }
                thread.setRunning(false);
                Intent intent = new Intent(getContext(), GameOver.class);
                intent.putExtra("winner", winner);
                getContext().startActivity(intent);
            }
        }
    }

    /**
     * Draws score bars on the bottom of the canvas.
     * They represent the number of sprites in each group.
     * @param canvas    Canvas to draw the score bars on.
     */
    private void drawScoreBars(Canvas canvas){
        float barResolution = (float) screenWidth/(groupSize*3);
        float rocksBar = barResolution * countSprites(rocks);
        float papersBar = barResolution * countSprites(papers);
        paint.setColor(Color.parseColor("#A6D0DD"));
        canvas.drawRect(0, bottomWall, rocksBar, screenHeight, paint);
        paint.setColor(Color.parseColor("#FFD3B0"));
        canvas.drawRect(rocksBar, bottomWall, rocksBar+papersBar, screenHeight, paint);
        paint.setColor(Color.parseColor("#FF6969"));
        canvas.drawRect(rocksBar+papersBar, bottomWall, screenWidth, screenHeight, paint);
    }

    /**
     * Draws all sprites on the canvas.
     * @param canvas Canvas to draw the sprites on.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(canvas == null){
            return;
        }
        canvas.drawColor(Color.parseColor("#FFFDF2"));
        drawScoreBars(canvas);
        for(Sprite sprite: allSprites) {
            if(sprite != null){
                sprite.draw(canvas);
            }
        }
    }
}

