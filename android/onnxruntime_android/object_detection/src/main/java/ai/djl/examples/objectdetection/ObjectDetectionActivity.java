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

package ai.djl.examples.objectdetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.android.core.BitmapImageFactory;
import ai.djl.examples.objectdetection.databinding.ActivityMainBinding;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.SemanticSegmentationTranslator;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class ObjectDetectionActivity extends AppCompatActivity implements CameraXConfig.Provider {

    private static final String TAG = ObjectDetectionActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST_CODE = 1;

    private PreviewView mViewFinder;
    private ImageView mImagePreview;
    private ImageButton mCloseImageButton;
    private ImageView mImagePredicted;
    private ProgressBar mProgressBar;
    private FloatingActionButton mCaptureButton;

    private ImageCapture imageCapture;
    private Bitmap bitmapBuffer;
    private String modelUrl = "file:///android_assert/yolov5s.onnx";

    ZooModel<Image, DetectedObjects> model;
    Predictor<Image, DetectedObjects> predictor;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      disable hybrid engine for OnnxRuntime
//        System.setProperty("ai.djl.onnx.disable_alternative", "true");
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mViewFinder = binding.viewFinder;
        mImagePreview = binding.imagePreview;
        mCloseImageButton = binding.closeImagePreview;
        mImagePredicted = binding.imagePredicted;
        mProgressBar = binding.progressBar;
        mCaptureButton = binding.captureButton;

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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(ObjectDetectionActivity.this);
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
                .setTargetResolution(new Size(640, 640))
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
        private void cacheFile(String source, String dest) {
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
        public void run() {
            try {
                cacheFile("yolov5s.onnx","yolov5s.onnx");
                cacheFile("synset.txt","synset.txt");
                model = ObjectDetectionModel.loadModel(getCacheDir().toPath().resolve("yolov5s.onnx"));
                predictor = model.newPredictor();
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), R.string.message_download_model_complete, Snackbar.LENGTH_LONG).show();
                });
            } catch (IOException | ModelException e) {
                Log.e(TAG, null, e);
                runOnUiThread(() -> {
                    mCaptureButton.setEnabled(false);
                    new AlertDialog.Builder(ObjectDetectionActivity.this)
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
                Image finalImg = img.duplicate();
                img = BitmapImageFactory.getInstance().fromNDArray(NDImageUtils.resize(img.toNDArray(NDManager.newBaseManager()),640, 640));
                DetectedObjects objects = predictor.predict(img);
                List<BoundingBox> boxes = new ArrayList<>();
                List<String> names = new ArrayList<>();
                List<Double> prob = new ArrayList<>();
                for (Classifications.Classification obj : objects.items()) {
                    DetectedObjects.DetectedObject objConvered = (DetectedObjects.DetectedObject) obj;
                    BoundingBox box = objConvered.getBoundingBox();
                    Rectangle rec = box.getBounds();
                    Rectangle rec2 = new Rectangle(
                            rec.getX() / 640,
                            rec.getY() / 640,
                            rec.getWidth() / 640,
                            rec.getHeight() / 640
                    );
                    boxes.add(rec2);
                    names.add(obj.getClassName());
                    prob.add(obj.getProbability());
                }
                DetectedObjects converted = new DetectedObjects(names, prob, boxes);
                finalImg.drawBoundingBoxes(converted);


                runOnUiThread(() -> {
                    mImagePredicted.setImageBitmap((Bitmap) finalImg.getWrappedImage());
                    mImagePredicted.setVisibility(View.VISIBLE);
                    mCloseImageButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                });
//            } catch (TranslateException e) {
            } catch (Exception e) {
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
