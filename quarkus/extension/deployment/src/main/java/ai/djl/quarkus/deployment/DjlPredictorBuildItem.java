package ai.djl.quarkus.deployment;

import ai.djl.inference.Predictor;
import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;

/**
 * A {@link io.quarkus.builder.item.BuildItem} holding a {@link Predictor}.
 */
public final class DjlPredictorBuildItem extends SimpleBuildItem {

    private final RuntimeValue<Predictor<?, ?>> value;

    public DjlPredictorBuildItem(RuntimeValue<Predictor<?, ?>> value) {
        this.value = value;
    }

    public RuntimeValue<Predictor<?, ?>> getValue() {
        return value;
    }

}
