package ai.djl.examples.semanticsegmentation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.stream.IntStream;

import ai.djl.ndarray.NDArray;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;


public class SemanticActivity extends AppCompatActivity implements Runnable {
    private ImageView mImageView;
    private Button mButtonSegment;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    private Image img;
    private String mImagename = "dog_bike_car.jpg";
    ZooModel<Image, Image> model;
    Predictor<Image, Image> predictor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mImagename));
            img = ImageFactory.getInstance().fromInputStream(getAssets().open(mImagename));
        } catch (IOException e) {
            Log.e("SemanticSegmentation", "Error reading assets", e);
            finish();
        }

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);

        final Button buttonRestart = findViewById(R.id.restartButton);
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mImagename == "deeplab.jpg")
                    mImagename = "dog.jpg";
                else
                    mImagename = "deeplab.jpg";
                try {
                    img = ImageFactory.getInstance().fromInputStream(getAssets().open(mImagename));
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mImagename));
                    mImageView.setImageBitmap(mBitmap);
                } catch (IOException e) {
                    Log.e("SemanticSegmentation", "Error reading assets", e);
                    finish();
                }
            }
        });

        mButtonSegment = findViewById(R.id.segmentButton);
        mProgressBar = findViewById(R.id.progressBar);
        mButtonSegment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonSegment.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonSegment.setText(getString(R.string.run_model));

                Thread thread = new Thread(SemanticActivity.this);
                thread.start();
            }
        });

        new UnpackTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class UnpackTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                model = SemanticModel.loadModel();
                predictor = model.newPredictor();
                return true;
            } catch (IOException | ModelException e) {
                Log.e("Semantic Segmentation", null, e);
            }
            return false;
        }
    }

    @Override
    public void run() {
        try (NDManager manager = NDManager.newBaseManager()) {
            Image image = predictor.predict(img);
            NDArray pixels = image.toNDArray(manager);
            Shape shape = pixels.getShape();
            int height = (int) shape.get(0);
            int width = (int) shape.get(1);
            int imageArea = width * height;

            Bitmap transferredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] raw = pixels.toUint8Array();
            IntStream.range(0, imageArea).parallel().forEach(ele -> {
                int x = ele % width;
                int y = ele / width;
                int red = raw[ele] & 0xFF;
                int green = raw[ele + imageArea] & 0xFF;
                int blue = raw[ele + imageArea * 2] & 0xFF;
                transferredBitmap.setPixel(x, y, Color.argb(255, red, green, blue));
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mImageView.setImageBitmap(transferredBitmap);
                    mButtonSegment.setEnabled(true);
                    mButtonSegment.setText(getString(R.string.segment));
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                }
            });

        } catch (TranslateException e) {
            e.printStackTrace();
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
}
