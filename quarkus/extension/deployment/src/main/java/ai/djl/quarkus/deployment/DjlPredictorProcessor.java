package ai.djl.quarkus.deployment;

import ai.djl.repository.zoo.ZooModel;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
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
    NativeImageResourceBuildItem nativeImageResourceBuildItem() {
        return new NativeImageResourceBuildItem("META-INF/extra.properties");
    }

    @BuildStep
    void runtimeInit(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInit) {
        for(String runtimeClass : RuntimeClasses.RUNTIME_CLASSES) {
            runtimeInit.produce(new RuntimeInitializedClassBuildItem(runtimeClass));
        }
    }

    @BuildStep
    void modelReflectionInit(BuildProducer<ReflectiveClassBuildItem> reflections, DjlModelConfiguration modelConfig) {
        reflections.produce(new ReflectiveClassBuildItem(true, true, modelConfig.inputClass));
        reflections.produce(new ReflectiveClassBuildItem(true, true, modelConfig.outputClass));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    DjlModelBuildItem createModel(DjlPredictorRecorder recorder, BeanContainerBuildItem beanContainerBuildItem,
            DjlModelConfiguration configuration)
            throws ClassNotFoundException, MalformedModelException, ModelNotFoundException, IOException {
        RuntimeValue<ZooModel<?, ?>> modelHolder = recorder.initializePredictor(configuration);
        recorder.configureDjlPredictorProducer(beanContainerBuildItem.getValue(), modelHolder);
        return new DjlModelBuildItem(modelHolder);
    }
}
