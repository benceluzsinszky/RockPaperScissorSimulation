package com.android.rockpaperscissors;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;


public class MainActivity extends Activity {
    private int groupSize;
    private int speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        groupSize = 20;
        speed = 2;

        Button button = findViewById(R.id.startButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        Slider groupSizeSlider = findViewById(R.id.groupSizeSlider);
        TextView groupSizeNumber = findViewById(R.id.groupSizeNumber);
        groupSizeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                groupSize = (int) value;
                groupSizeNumber.setText(String.valueOf((int) value));
            }
        });

        Slider speedSlider = findViewById(R.id.speedSlider);
        TextView speedNumber = findViewById(R.id.speedNumber);
        speedSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                speed = (int) value;
                speedNumber.setText(String.valueOf((int) value));;
            }
        });

        ImageView rotatingImage = findViewById(R.id.rotatingImage);

        RotateAnimation anim = new RotateAnimation(
                0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.52f)
                ;
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(50000);
        rotatingImage.startAnimation(anim);

    }

    @Override
    public void onBackPressed() {}
    public void startGame(){
        setContentView(new GameView(this, groupSize, speed));
    }
}