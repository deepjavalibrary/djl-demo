package com.example.quickdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button recognition = findViewById(R.id.doodle_recog);
        recognition.setOnClickListener(new RecognitionListener());
    }

    class RecognitionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            System.out.println("Switch to doodle recognition");

            Intent doodleRecognition = new Intent(getApplicationContext(), DoodleRecognitionActivity.class);
            startActivity(doodleRecognition);
        }
    }
}
