package ai.djl.examples.semanticsegmentation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;


public class SemanticActivity extends AppCompatActivity {
    private ImageView mImageView;
    private Button mButtonSegment;
    private ProgressBar mProgressBar;
    private Image img;
    private String mImageName;
    ZooModel<Image, Image> model;
    Predictor<Image, Image> predictor;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title);
        }

        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);
        Button buttonRestart = findViewById(R.id.restartButton);
        buttonRestart.setOnClickListener(v -> {
            if ("dog_bike_car.jpg".equals(mImageName)) {
                setImageView("dog.jpg");
            } else {
                setImageView("dog_bike_car.jpg");
            }
        });
        mButtonSegment = findViewById(R.id.segmentButton);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mButtonSegment.setEnabled(false);
        mButtonSegment.setOnClickListener(v -> {
            mButtonSegment.setEnabled(false);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mButtonSegment.setText(getString(R.string.run_model));

            executor.execute(new InferenceTask());
        });

        setImageView("dog_bike_car.jpg");

        mButtonSegment.setText(getString(R.string.download_model));
        executor.execute(new LoadModelTask());
    }

    private void setImageView(String imageName) {
        try {
            mImageName = imageName;
            Bitmap mBitmap = BitmapFactory.decodeStream(getAssets().open(mImageName));
            mImageView.setImageBitmap(mBitmap);
            img = ImageFactory.getInstance().fromInputStream(getAssets().open(mImageName));
        } catch (IOException e) {
            Log.e("SemanticSegmentation", "Error reading assets", e);
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

    private class LoadModelTask implements Runnable {

        @Override
        public void run() {
            try {
                model = SemanticModel.loadModel();
                predictor = model.newPredictor();
                runOnUiThread(() -> {
                    mButtonSegment.setText(getString(R.string.segment));
                    mButtonSegment.setEnabled(true);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                });
            } catch (IOException | ModelException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(SemanticActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Failed to load model");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> finish());
            }
        }
    }

    private class InferenceTask implements Runnable {

        @Override
        public void run() {
            try {
                Bitmap transferredBitmap = (Bitmap) predictor.predict(img).getWrappedImage();
                runOnUiThread(() -> {
                    mImageView.setImageBitmap(transferredBitmap);
                    mButtonSegment.setText(getString(R.string.segment));
                    mButtonSegment.setEnabled(true);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                });
            } catch (TranslateException e) {
                Log.e("SemanticSegmentation", null, e);
            }
        }
    }
}
