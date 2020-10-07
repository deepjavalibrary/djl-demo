package ai.djl.quarkus.deployment;

import ai.djl.repository.zoo.ZooModel;
import java.io.IOException;

import ai.djl.MalformedModelException;
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
    DjlModelBuildItem createModel(DjlPredictorRecorder recorder, BeanContainerBuildItem beanContainerBuildItem,
            DjlModelConfiguration configuration)
            throws ClassNotFoundException, MalformedModelException, ModelNotFoundException, IOException {
        RuntimeValue<ZooModel<?, ?>> modelHolder = recorder.initializePredictor(configuration);
        recorder.configureDjlPredictorProducer(beanContainerBuildItem.getValue(), modelHolder);
        return new DjlModelBuildItem(modelHolder);
    }
}
