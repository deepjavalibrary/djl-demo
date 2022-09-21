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

package ai.djl.examples.semanticsegmentation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.examples.semanticsegmentation.databinding.ActivityMainBinding;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class SemanticActivity extends AppCompatActivity implements CameraXConfig.Provider {

    private static final String TAG = SemanticActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST_CODE = 1;

    private PreviewView mViewFinder;
    private ImageView mImagePreview;
    private ImageButton mCloseImageButton;
    private ImageView mImagePredicted;
    private ProgressBar mProgressBar;
    private FloatingActionButton mCaptureButton;
    private SwitchCompat mSwitch;

    private ImageCapture imageCapture;
    private Bitmap bitmapBuffer;
    boolean isUseSelfTranslator = false;

    ZooModel<Image, Image> model;
    Predictor<Image, Image> predictor;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mViewFinder = binding.viewFinder;
        mImagePreview = binding.imagePreview;
        mCloseImageButton = binding.closeImagePreview;
        mImagePredicted = binding.imagePredicted;
        mProgressBar = binding.progressBar;
        mCaptureButton = binding.captureButton;
        mSwitch = binding.useSelfTranslatorSwitch;
        mSwitch.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                isUseSelfTranslator = mSwitch.isChecked();
                executor.execute(new LoadModelTask());
                return false;
            }
        });

        mCaptureButton.setOnClickListener(view -> {

            mCaptureButton.setEnabled(false);
            mImagePredicted.setVisibility(View.GONE);

            if (imageCapture != null) {
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {

                            @Override
                            public void onCaptureSuccess(@NonNull ImageProxy image) {
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                bitmapBuffer = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                image.close();

                                // Rotate image if needed
                                if (image.getImageInfo().getRotationDegrees() != 0) {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(image.getImageInfo().getRotationDegrees());
                                    bitmapBuffer = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);
                                }

                                runOnUiThread(() -> {
                                    mViewFinder.setVisibility(View.INVISIBLE);
                                    mImagePreview.setImageBitmap(bitmapBuffer);
                                    mImagePreview.setVisibility(View.VISIBLE);
                                    mProgressBar.setVisibility(View.VISIBLE);
                                });

                                executor.execute(new InferenceTask());
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.e(TAG, "Photo capture failed: " + exception.getMessage());
                            }
                        }
                );
            }
        });

        mCloseImageButton.setOnClickListener(view -> {
            mCloseImageButton.setVisibility(View.GONE);
            mViewFinder.setVisibility(View.VISIBLE);
            mImagePreview.setVisibility(View.GONE);
            mImagePredicted.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mCaptureButton.setEnabled(true);
        });

        Snackbar.make(findViewById(android.R.id.content), R.string.message_download_model,
                Snackbar.LENGTH_LONG).show();
        //first initialize
        executor.execute(new LoadModelTask());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Request permissions each time the app resumes, since they can be revoked at any time
        if (checkCameraPermission()) {
            setUpCamera();
        } else {
            requestCameraPermission();
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpCamera();
            } else {
                mCaptureButton.setEnabled(false);
            }
        }
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    /**
     * Check for camera permission
     */
    private Boolean checkCameraPermission() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission
     */
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(SemanticActivity.this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(250, 250))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll();

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            preview.setSurfaceProvider(mViewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private class LoadModelTask implements Runnable {

        @Override
        public void run() {
            try {
                isUseSelfTranslator =  mSwitch.isChecked();
                model = SemanticModel.loadModel(isUseSelfTranslator);
                predictor = model.newPredictor();
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_download_model_complete, Snackbar.LENGTH_LONG).show();
                });
            } catch (IOException | ModelException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(false);
                    new AlertDialog.Builder(SemanticActivity.this)
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
            try {
                // Predict
                Image img = ImageFactory.getInstance().fromImage(bitmapBuffer);
                Bitmap transferredBitmap = (Bitmap) predictor.predict(img).getWrappedImage();
                runOnUiThread(() -> {
                    mImagePredicted.setImageBitmap(transferredBitmap);
                    mImagePredicted.setVisibility(View.VISIBLE);
                    mCloseImageButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                });
            } catch (TranslateException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Inference failed.", Snackbar.LENGTH_LONG).show();
                });
            }
        }
    }
}
