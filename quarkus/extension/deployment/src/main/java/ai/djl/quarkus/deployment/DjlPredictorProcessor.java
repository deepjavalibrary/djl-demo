package ai.djl.quarkus.deployment;

import java.io.IOException;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ModelNotFoundException;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import ai.djl.quarkus.runtime.DjlModelConfiguration;
import ai.djl.quarkus.runtime.DjlPredictorRecorder;
import io.quarkus.runtime.RuntimeValue;

/**
 * The Quarkus main class for the DJL extension.
 */
class DjlPredictorProcessor {

    private static final String FEATURE = "djl";

    @BuildStep
    FeatureBuildItem createFeature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    DjlPredictorBuildItem createPredictor(DjlPredictorRecorder recorder, BeanContainerBuildItem beanContainerBuildItem,
            DjlModelConfiguration configuration)
            throws ClassNotFoundException, MalformedModelException, ModelNotFoundException, IOException {
        RuntimeValue<Predictor<?, ?>> predictorHolder = recorder.initializePredictor(configuration);
        recorder.configureDjlPredictorProducer(beanContainerBuildItem.getValue(), predictorHolder);
        return new DjlPredictorBuildItem(predictorHolder);
    }
}
