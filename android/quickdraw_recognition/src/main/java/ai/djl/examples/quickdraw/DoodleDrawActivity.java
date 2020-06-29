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

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.ZooModel;

public class DoodleDrawActivity extends AppCompatActivity implements View.OnClickListener {

    private PaintView paintView;
    View progressBar;
    View containerView;
    ImageView imageView;
    TextView textView;
    ZooModel<Image, Classifications> model;
    Predictor<Image, Classifications> predictor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title);
        }

        setContentView(R.layout.activity_doodle);
        progressBar = findViewById(R.id.loading_progress);
        containerView = findViewById(R.id.container);
        paintView = findViewById(R.id.paintView);
        imageView = findViewById(R.id.doodle);
        textView = findViewById(R.id.msgText);

        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(this);
        Button share = findViewById(R.id.share);
        share.setOnClickListener(this);

        new UnpackTask().execute();
    }

    @Override
    protected void onDestroy() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.clear) {
            textView.setText("");
            paintView.clear();
        } else if (viewId == R.id.share) {
            paintView.share();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UnpackTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                model = DoodleModel.loadModel();
                predictor = model.newPredictor();
                return true;
            } catch (IOException | ModelException e) {
                Log.e("DoodleDraw", null, e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                paintView.init(metrics, imageView, textView, predictor);
                progressBar.setVisibility(View.GONE);
                containerView.setVisibility(View.VISIBLE);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(DoodleDrawActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Failed to load model");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> finish());
            }
        }
    }
}
