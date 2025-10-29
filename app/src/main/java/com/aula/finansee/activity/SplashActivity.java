package com.aula.finansee.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.finansee.R;

public class SplashActivity extends AppCompatActivity {

    private static final int ANIMATION_DURATION = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar layout imediatamente
        setContentView(R.layout.activity_splash);

        ImageView logoImageView = findViewById(R.id.logoView);

        // Animação de flip contínuo
        ObjectAnimator flipAnimator = ObjectAnimator.ofFloat(logoImageView, "rotationY", 0f, 360f);
        flipAnimator.setDuration(1000); // 1 segundo por giro
        flipAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flipAnimator.start();

        // Abrir MainActivity após ANIMATION_DURATION
        logoImageView.postDelayed(() -> {
            flipAnimator.cancel(); // parar animação
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, ANIMATION_DURATION);
    }
}
