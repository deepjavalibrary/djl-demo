/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package ai.djl.examples.neuralmachinetranslation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.examples.neuralmachinetranslation.databinding.ActivityMainBinding;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.ZooModel;

public class NMTActivity extends AppCompatActivity {

    private static final String TAG = NMTActivity.class.getSimpleName();

    static final Gson GSON = new Gson();

    private EditText mEditText;
    private TextView mTextView;
    private Button mButton;

    private ZooModel<NDList, NDList> encoderModel;
    private ZooModel<NDList, NDList> decoderModel;
    private LinkedTreeMap<String, Long> encoderWords;
    private LinkedTreeMap<String, String> decoderWords;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mButton = binding.btnTranslate;
        mEditText = binding.etFrom;
        mTextView = binding.tvTo;
        mButton.setEnabled(false);
        mButton.setOnClickListener(v -> {
            mButton.setEnabled(false);
            mButton.setText(R.string.button_run_model);
            executor.execute(new InferenceTask());
        });

        mButton.setText(R.string.button_download_model);
        executor.execute(new LoadModelTask());
    }

    @Override
    protected void onDestroy() {
        if (encoderModel != null) {
            encoderModel.close();
        }
        if (decoderModel != null) {
            decoderModel.close();
        }
        super.onDestroy();
    }

    private class LoadModelTask implements Runnable {

        @Override
        public void run() {
            try {
                encoderModel = NeuralModel.loadModelEncoder();
                decoderModel = NeuralModel.loadModelDecoder();

                try (Reader reader = new InputStreamReader(
                        getAssets().open("source_wrd2idx.json"), StandardCharsets.UTF_8)) {
                    Type mapType = new TypeToken<Map<String, Long>>() {}.getType();
                    encoderWords = GSON.fromJson(reader, mapType);
                }

                try (Reader reader = new InputStreamReader(
                        getAssets().open("target_idx2wrd.json"), StandardCharsets.UTF_8)) {
                    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
                    decoderWords = GSON.fromJson(reader, mapType);
                }

                runOnUiThread(() -> {
                    mButton.setText(getString(R.string.button_translate));
                    mButton.setEnabled(true);
                });
            } catch (IOException | ModelException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    new AlertDialog.Builder(NMTActivity.this)
                            .setTitle(R.string.dialog_title)
                            .setMessage(R.string.dialog_message)
                            .setNeutralButton(android.R.string.ok, (dialog, which) -> finish())
                            .show();
                });
            }
        }
    }

    private class InferenceTask implements Runnable {

        @Override
        public void run() {
            String result = translate(mEditText.getText().toString());
            runOnUiThread(() -> {
                mTextView.setText(result);
                mButton.setText(R.string.button_translate);
                mButton.setEnabled(true);
            });
        }
    }

    private String translate(final String text) {
        if (TextUtils.isEmpty(text)) {
            runOnUiThread(() -> {
                Toast.makeText(NMTActivity.this, "No input for inference.", Toast.LENGTH_LONG)
                        .show();
                mButton.setText(getString(R.string.button_translate));
                mButton.setEnabled(true);
            });
            return "";
        } else {
            try (NDManager manager = encoderModel.getNDManager().newSubManager()) {
                NDList list = NeuralModel.predictEncoder(text, encoderModel, encoderWords, manager);
                return NeuralModel.predictDecoder(list, decoderModel, decoderWords, manager);
            } catch (ModelException | IOException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    Toast.makeText(NMTActivity.this, "Inference failed. " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                    mButton.setText(getString(R.string.button_translate));
                    mButton.setEnabled(true);
                });
                return "";
            }
        }
    }
}
