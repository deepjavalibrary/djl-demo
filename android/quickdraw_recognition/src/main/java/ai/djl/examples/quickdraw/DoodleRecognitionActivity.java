/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package ai.djl.examples.quickdraw;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DoodleRecognitionActivity extends AppCompatActivity implements View.OnClickListener {

    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doodle_recognition);
        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        paintView.clear();
    }

}
