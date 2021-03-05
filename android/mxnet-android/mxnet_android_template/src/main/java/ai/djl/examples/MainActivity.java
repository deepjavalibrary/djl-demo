package ai.djl.examples;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class MainActivity extends AppCompatActivity {

    Predictor<Image, Classifications> predictor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File dir = getFilesDir();
        System.setProperty("DJL_CACHE_DIR", dir.getAbsolutePath());

        new LoadModel().execute();
    }

    @SuppressLint("StaticFieldLeak")
    class LoadModel extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                        .addTransform(new ToTensor())
                        .optFlag(Image.Flag.COLOR)
                        .optApplySoftmax(true)
                        .build();
                Criteria<Image, Classifications> criteria =
                        Criteria.builder()
                                .setTypes(Image.class, Classifications.class)
                                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                                .optArtifactId("ai.djl.mxnet:mobilenet")
                                .optTranslator(translator)
                                .build();
                ZooModel<Image, Classifications> model = ModelZoo.loadModel(criteria);
                predictor = model.newPredictor();
                return true;
            } catch (IOException | ModelException e) {
                Log.e("LoadModel", null, e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                TextView textView = findViewById(R.id.msgText);
                try (NDManager manager = NDManager.newBaseManager()) {
                    Image image = ImageFactory.getInstance().fromNDArray(manager.zeros(new Shape(3, 224, 224), DataType.UINT8));
                    Classifications res = predictor.predict(image);
                    Log.i("MainActivity", "Predict Result: \n" + res);
                    textView.setText(res.toString());
                } catch (TranslateException e) {
                    Log.e("LoadModel", null, e);
                }
            }
        }
    }
}