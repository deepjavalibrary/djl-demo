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
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.examples.semanticsegmentation.databinding.ActivityMainBinding;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.CategoryMask;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class SemanticActivity extends AppCompatActivity implements CameraXConfig.Provider, AdapterView.OnItemSelectedListener {

    private static final String TAG = SemanticActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST_CODE = 1;

    private static final int CLASS_ALL = -1;
    private static final int CLASS_BACKGROUND = 0;

    private static final int OPTION_HIGHLIGHT = 0;
    private static final int OPTION_EXTRACT = 1;

    private PreviewView mViewFinder;
    private ImageView mImagePreview;
    private ImageView mImagePredicted;
    private ProgressBar mProgressBar;
    private Spinner mClassSpinner;
    private Spinner mOptionSpinner;
    private EditText mColorText;
    private AppCompatSeekBar mOpacitySeekbar;
    private FloatingActionButton mCaptureButton;
    private FloatingActionButton mCloseButton;

    private ImageCapture imageCapture;
    private Bitmap bitmapBuffer;

    ZooModel<Image, CategoryMask> model;
    Predictor<Image, CategoryMask> predictor;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mViewFinder = binding.viewFinder;
        mImagePreview = binding.imagePreview;
        mImagePredicted = binding.imagePredicted;
        mProgressBar = binding.progressBar;
        mClassSpinner = binding.classSpinner;
        mOptionSpinner = binding.optionSpinner;
        mColorText = binding.colorEdittext;
        mOpacitySeekbar = binding.opacitySeekbar;
        mCaptureButton = binding.captureButton;
        mCloseButton = binding.closeButton;

        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource
                (this, R.array.class_array, android.R.layout.simple_spinner_item);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mClassSpinner.setAdapter(classAdapter);
        mClassSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> optionAdapter = ArrayAdapter.createFromResource
                (this, R.array.option_array1, android.R.layout.simple_spinner_item);
        optionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOptionSpinner.setAdapter(optionAdapter);

        mCaptureButton.setOnClickListener(view -> {
            mCaptureButton.setEnabled(false);
            mImagePredicted.setVisibility(View.INVISIBLE);
            mClassSpinner.setEnabled(false);
            mOptionSpinner.setEnabled(false);
            mColorText.setEnabled(false);
            mOpacitySeekbar.setEnabled(false);

            if (imageCapture != null) {
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {

                            @Override
                            public void onCaptureSuccess(@NonNull ImageProxy image) {
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                if (bitmapBuffer != null) {
                                    bitmapBuffer.recycle();
                                }
                                bitmapBuffer = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                image.close();

                                // Rotate image if needed
                                if (image.getImageInfo().getRotationDegrees() != 0) {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(image.getImageInfo().getRotationDegrees());
                                    Bitmap oldBitmap = bitmapBuffer;
                                    bitmapBuffer = Bitmap.createBitmap(bitmapBuffer, 0, 0,
                                            bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);
                                    oldBitmap.recycle();
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

        mCloseButton.setOnClickListener(view -> {
            mCloseButton.setVisibility(View.GONE);
            mViewFinder.setVisibility(View.VISIBLE);
            mImagePreview.setVisibility(View.GONE);
            mImagePredicted.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mClassSpinner.setEnabled(true);
            mOptionSpinner.setEnabled(true);
            mColorText.setEnabled(true);
            mOpacitySeekbar.setEnabled(true);
            mCaptureButton.setEnabled(true);
            mCaptureButton.setVisibility(View.VISIBLE);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.class_spinner) {
            switch (i - 1) {
                case CLASS_ALL:
                    mOptionSpinner.setAdapter(ArrayAdapter.createFromResource
                            (this, R.array.option_array1, android.R.layout.simple_spinner_item));
                    break;
                case CLASS_BACKGROUND:
                    mOptionSpinner.setAdapter(ArrayAdapter.createFromResource
                            (this, R.array.option_array2, android.R.layout.simple_spinner_item));
                    break;
                default:
                    mOptionSpinner.setAdapter(ArrayAdapter.createFromResource
                            (this, R.array.option_array3, android.R.layout.simple_spinner_item));
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(SemanticActivity.this);
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
                model = SemanticModel.loadModel();
                predictor = model.newPredictor();
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content),
                            R.string.message_download_model_complete, Snackbar.LENGTH_LONG).show();
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
                CategoryMask mask = predictor.predict(img);

                int opacity = mOpacitySeekbar.getProgress();
                int classId = mClassSpinner.getSelectedItemPosition() - 1;

                final Bitmap segmentBitmap;
                if (mOptionSpinner.getSelectedItemPosition() == OPTION_HIGHLIGHT) {
                    if (classId < 0) {
                        mask.drawMask(img, opacity, 0);
                    } else {
                        if (TextUtils.isEmpty(mColorText.getText())) {
                            mColorText.setText(R.string.default_color);
                        }
                        mask.drawMask(img, classId,
                                Integer.parseInt(mColorText.getText().toString(), 16), opacity);
                    }
                    segmentBitmap = (Bitmap) img.getWrappedImage();
                } else if (mOptionSpinner.getSelectedItemPosition() == OPTION_EXTRACT) {
                    Image maskImage = mask.getMaskImage(img, classId);
                    maskImage = maskImage.resize(img.getWidth(), img.getHeight(), true);
                    segmentBitmap = (Bitmap) maskImage.getWrappedImage();
                } else {
                    Image maskImage = mask.getMaskImage(img, classId);
                    maskImage = maskImage.resize(img.getWidth(), img.getHeight(), true);
                    Image bg = ImageFactory.getInstance().fromUrl(new URL("https://images.pexels.com/photos/924824/pexels-photo-924824.jpeg"));
                    bg = bg.resize(img.getWidth(), img.getHeight(), true);
                    bg.drawImage(maskImage, true);
                    segmentBitmap = (Bitmap) bg.getWrappedImage();
                }

                runOnUiThread(() -> {
                    mImagePredicted.setImageBitmap(segmentBitmap);
                    mImagePredicted.setVisibility(View.VISIBLE);
                    mCaptureButton.setVisibility(View.GONE);
                    mCloseButton.setEnabled(true);
                    mCloseButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                });
            } catch (TranslateException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_inference_failed,
                            Snackbar.LENGTH_LONG).show();
                });
            } catch (MalformedURLException e) {
                Log.d(TAG, "MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e.getMessage());
            }
        }
    }
}
