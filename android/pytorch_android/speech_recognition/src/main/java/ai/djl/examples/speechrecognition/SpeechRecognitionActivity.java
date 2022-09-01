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

package ai.djl.examples.speechrecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class SpeechRecognitionActivity extends AppCompatActivity implements Runnable {

    private static final String TAG = SpeechRecognitionActivity.class.getName();

    private TextView mTextView;
    private Button mButton;

    ZooModel<Audio, String> model;
    Predictor<Audio, String> predictor;
    Executor executor = Executors.newSingleThreadExecutor();

    private final static int REQUEST_RECORD_AUDIO = 13;
    private final static int AUDIO_LEN_IN_SECOND = 6;
    private final static int SAMPLE_RATE = 16000;
    private final static int RECORDING_LENGTH = SAMPLE_RATE * AUDIO_LEN_IN_SECOND;

    private final static String LOG_TAG = SpeechRecognitionActivity.class.getSimpleName();

    private int mStart = 1;
    private HandlerThread mTimerThread;
    private Handler mTimerHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerHandler.postDelayed(mRunnable, 1000);

            SpeechRecognitionActivity.this.runOnUiThread(() -> {
                mButton.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND - mStart));
                mStart += 1;
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.btnRecognize);
        mTextView = findViewById(R.id.tvResult);

        mButton.setOnClickListener(v -> {
            mButton.setText(String.format("Listening - %ds left", AUDIO_LEN_IN_SECOND));
            mButton.setEnabled(false);

            Thread thread = new Thread(SpeechRecognitionActivity.this);
            thread.start();

            mTimerThread = new HandlerThread("Timer");
            mTimerThread.start();
            mTimerHandler = new Handler(mTimerThread.getLooper());
            mTimerHandler.postDelayed(mRunnable, 1000);

        });
        requestMicrophonePermission();
        mButton.setText(getString(R.string.loader));
        mButton.setEnabled(false);
        executor.execute(new LoadModelTask());
    }

    @Override
    protected void onDestroy() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        stopTimerThread();
        super.onDestroy();
    }

    protected void stopTimerThread() {
        mTimerThread.quitSafely();
        try {
            mTimerThread.join();
            mTimerThread = null;
            mTimerHandler = null;
            mStart = 1;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error on stopping background thread", e);
        }
    }

    private class LoadModelTask implements Runnable {

        @Override
        public void run() {
            try {
                model = SpeechRecognitionModel.loadModel();
                predictor = model.newPredictor();
                runOnUiThread(() -> {
                    mButton.setText("Start");
                    mButton.setEnabled(true);
                });
            } catch (IOException | ModelException e) {
                Log.e("SpeechRecognition", null, e);
                runOnUiThread(() -> {
                    AlertDialog alertDialog =
                            new AlertDialog.Builder(SpeechRecognitionActivity.this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Failed to load model");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> finish());
                });
            }
        }
    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        // check for audio input permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();

        long shortsRead = 0;
        int recordingOffset = 0;
        short[] audioBuffer = new short[bufferSize / 2];
        short[] recordingBuffer = new short[RECORDING_LENGTH];

        // listen to live audio data and save it
        while (shortsRead < RECORDING_LENGTH) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
            shortsRead += numberOfShort;
            System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, numberOfShort);
            recordingOffset += numberOfShort;
        }

        record.stop();
        record.release();
        stopTimerThread();

        runOnUiThread(() -> {
            mButton.setText("Recognizing...");
        });

        float[] floatInputBuffer = new float[RECORDING_LENGTH];

        // feed in float values between -1.0f and 1.0f by dividing the signed 16-bit inputs.
        for (int i = 0; i < RECORDING_LENGTH; i++) {
            floatInputBuffer[i] = recordingBuffer[i] / (float) Short.MAX_VALUE;
        }

        String output = "";
        try {
            Audio audio = AudioFactory.getInstance().fromData(floatInputBuffer);
            output = predictor.predict(audio);
        } catch (TranslateException e) {
            Log.e(LOG_TAG, null, e);
        }

        final String result = output;
        runOnUiThread(() -> {
            mTextView.setText(result);
            mButton.setEnabled(true);
            mButton.setText("Start");
        });
    }
}