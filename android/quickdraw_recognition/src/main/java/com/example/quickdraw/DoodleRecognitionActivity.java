package com.example.quickdraw;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DoodleRecognitionActivity extends AppCompatActivity {

    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle_recognition);
        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(new ClearListener());
        paintView = findViewById(R.id.paintView);
        ImageView imageView = findViewById(R.id.doodle);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        unzipFile("models/doodle_mobilenet.pt", "doodle_mobilenet.pt");
        unzipFile("models/synset.txt", "synset.txt");
        paintView.init(metrics, imageView, getCacheDir().toPath());
        getAssets().getLocales();
    }

    private void unzipFile(String source, String dest) {
        File f = getCacheDir().toPath().resolve(dest).toFile();
        if (!f.exists()) try {
            InputStream is = getAssets().open(source);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class ClearListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            paintView.clear();
        }
    }

}
