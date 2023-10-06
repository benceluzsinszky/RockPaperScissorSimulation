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

    /**
     * Create a single sprite.
     * @param size          X,Y dimensions of the sprite.
     * @param image         Bitmap used to draw the sprite.
     * @param x             Spawning X coordinate.
     * @param y             Spawning Y coordinate.
     * @param ownGroup      Group of the sprite.
     * @param hunterGroup   Hunter group of the sprite (the sprites that can consume it).
     * @param preyGroup     Prey group for the sprite (the sprites it consumes).
     * @param speed         Movement speed of the sprite.
     */
    public Sprite(int size, Bitmap image, int x, int y, Sprite[] ownGroup, Sprite[] hunterGroup, Sprite[] preyGroup, int speed) {
        this.size = size;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.ownGroup = ownGroup;
        this.hunterGroup = hunterGroup;
        this.preyGroup = preyGroup;
        this.image = image;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        bottomWall = screenHeight - screenHeight /15;
    }

    /**
     * Gets the coordinates of the sprite as an array of floats.
     *
     * @return An array containing the X and Y coordinates of the sprite.
     */
    public float[] getCoordinates() {
        return new float[]{x, y};
    }

    /**
     * Sets the image for the sprite.
     *
     * @param image The Bitmap image to set for the sprite.
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }

    /**
     * Sets the group of sprites to which this sprite belongs.
     *
     * @param ownGroup The Sprite array representing the group of sprites to set.
     */
    public void setOwnGroup(Sprite[] ownGroup) {
        this.ownGroup = ownGroup;
    }

    /**
     * Sets the group of sprites that are hunting this sprite.
     *
     * @param hunterGroup The Sprite array representing the group of hunting sprites to set.
     */
    public void setHunterGroup(Sprite[] hunterGroup) {
        this.hunterGroup = hunterGroup;
    }

    /**
     * Sets the group of sprites that this sprite is hunting.
     *
     * @param preyGroup The Sprite array representing the group of prey sprites to set.
     */
    public void setPreyGroup(Sprite[] preyGroup) {
        this.preyGroup = preyGroup;
    }

    /**
     * Draws the sprite's image onto the Canvas at the current (x, y) coordinates.
     *
     * @param canvas The Canvas on which to draw the sprite's image.
     */
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

    /**
     * Moves the sprite by performing random movement, hunting, and running behaviors.
     * This method should be called to update the sprite's position.
     */
    private void moveSprite() {
        randomMovement();
        closestHunter = getInfo(hunterGroup);
        closestPrey = getInfo(preyGroup);
        hunt();
        run();
    }

    /**
     * Generates random movement for the sprite within its speed range.
     */
    private void randomMovement() {
        double randomX = ThreadLocalRandom.current().nextDouble(speed * -1, speed);
        double randomY = ThreadLocalRandom.current().nextDouble(speed * -1, speed);
        x += randomX;
        y += randomY;
    }

    /**
     * Finds and returns the closest sprite from a given group.
     *
     * @param group The group of sprites from which to find the closest sprite.
     * @return The closest sprite in the group, or null if the group is empty or null.
     */
    private Sprite getInfo(Sprite[] group) {
        Sprite closestSprite = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Sprite sprite : group) {
            if (sprite == this || sprite == null) {
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

    private void predatorPreyBehavior(){
        // TODO: make a single hunt-run method
    }

    /**
     * Moves the sprite towards the direction of its closest prey.
     */
    private void hunt() {
        if (closestPrey != null) {
            double dX = (closestPrey.x + (size * 0.5) - x);
            double dY = (closestPrey.y + (size * 0.5) - y);
            double distance = Math.sqrt(Math.pow(dY, 2) + Math.pow(dX, 2));
            x += (dX / distance) * speed;
            y += (dY / distance) * speed;
        }
    }

    /**
     * Makes the sprite to run away from its closest hunter if it exists and is within a certain distance.
     * The sprite tries to maintain a safe distance from the hunter.
     */
    private void run() {
        if (closestHunter == null) {
            return;
        }
        double dX = (closestHunter.x + (size * 0.5) - x);
        double dY = (closestHunter.y + (size * 0.5) - y);
        double distance = Math.sqrt(Math.pow(dY, 2) + Math.pow(dX, 2));
        if (distance > 500) {
            return;
        }
        x -= (dX / distance) * speed * 0.95;
        y -= (dY / distance) * speed * 0.95;
    }

    /**
     * This method prevents the sprite from moving outside the visible area.
     * If the sprite touches the left, right, top, or bottom wall of the screen,
     * its position is adjusted accordingly.
     */
    private void checkWalls() {
        // Check and adjust for the left wall
        if (x <= 20) {
            x = 20;
        }

        // Check and adjust for the right wall
        if (x + size >= screenWidth - 20) {
            x = screenWidth - 20 - size;
        }

        // Check and adjust for the top wall
        if (y <= 20) {
            y = 20;
        }

        // Check and adjust for the bottom wall
        if (y + size >= bottomWall - 20) {
            y = bottomWall - 20 - size;
        }
    }

    /**
     * Applies wall avoidance behavior to the sprite's movement so it does not stick on the screen boundaries.
     * The method uses {@code avoidanceWeight} to control the amount of avoidance.
     */
    private void wallAvoidance() {
        // Only avoid walls in the outer quarters
        if (!(x + (size * 0.5) < screenWidth * 0.25 || x + (size * 0.5) > screenWidth * 0.75 ||
                y + (size * 0.5) < bottomWall * 0.25 || y + (size * 0.5) > bottomWall * 0.75)) {
            return;
        }

        // Define the avoidance weight
        double avoidanceWeight = 0.002;

        // Initialize direction flags for avoidance in X and Y directions
        int dirX = 1;
        double avoidanceX = screenWidth * 0.5 - x + (size * 0.5);
        int dirY = 1;
        double avoidanceY = bottomWall * 0.5 - y + (size * 0.5);

        // Determine avoidance direction based on sprite's position
        if (x + (size * 0.5) >= screenWidth * 0.5) {
            dirX = -1;
            avoidanceX = x + (size * 0.5) - screenWidth * 0.5;
        }
        if (y + (size * 0.5) >= bottomWall * 0.5) {
            dirY = -1;
            avoidanceY = y + (size * 0.5) - bottomWall * 0.5;
        }

        // Apply avoidance by adjusting sprite's position
        x += dirX * avoidanceX * avoidanceWeight;
        y += dirY * avoidanceY * avoidanceWeight;
    }

    /**
     * Handles self-collision detection and resolution for the sprite within its own group.
     * If the sprite collides with any other sprite in its own group, it adjusts its position
     * to prevent overlap and maintain separation.
     */
    private void selfCollision() {
        for (Sprite sprite : ownGroup) {
            if (sprite == this || sprite == null) {
                continue;
            }
            float[] spriteCoordinates = sprite.getCoordinates();
            float distanceX = x - spriteCoordinates[0];
            float distanceY = y - spriteCoordinates[1];

            // Check if both distances are smaller than or equal to the sprite's size, indicating a collision
            if (!(Math.abs(distanceY) <= size && Math.abs(distanceX) <= size)) {
                continue;
            }

            // Handle collision resolution based on relative positions
            if (distanceX > 0) {
                x += speed; // Move to the right
            } else if (distanceX < 0) {
                x -= speed; // Move to the left
            }

            if (distanceY > 0) {
                y += speed; // Move down
            } else if (distanceY < 0) {
                y -= speed; // Move up
            }
        }
    }

    /**
     * Implements the eating behavior of the sprite, where it consumes prey sprites that are within its reach.
     * If a prey sprite is consumed, it is removed from the prey group, and the sprite taking the prey's place
     * becomes a part of the sprite's own group.
     */
    private void eat() {
        for (int i = 0; i < preyGroup.length; i++) {
            Sprite sprite = preyGroup[i];
            if (sprite == null) {
                continue;
            }
            float[] spriteCoordinates = sprite.getCoordinates();
            double distanceX = x - spriteCoordinates[0];
            double distanceY = y - spriteCoordinates[1];
            double hitBox = size * 0.7;

            // Check if both distances are smaller than or equal to the hitbox size, indicating successful eating
            if (!(Math.abs(distanceY) <= hitBox && Math.abs(distanceX) <= hitBox)) {
                continue;
            }

            // Remove the eaten prey sprite from the prey group
            preyGroup[i] = null;

            // Add the sprite to the sprite's own group and configure its properties
            ownGroup[i] = sprite;
            sprite.setImage(image);
            sprite.setOwnGroup(ownGroup);
            sprite.setHunterGroup(hunterGroup);
            sprite.setPreyGroup(preyGroup);
        }
    }
}
