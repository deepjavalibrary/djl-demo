package ai.djl.quarkus.deployment;

import ai.djl.repository.zoo.ZooModel;
import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;

/**
 * A {@link io.quarkus.builder.item.BuildItem} holding a {@link ZooModel}.
 */
public final class DjlModelBuildItem extends SimpleBuildItem {

    private final RuntimeValue<ZooModel<?, ?>> value;

    public DjlModelBuildItem(RuntimeValue<ZooModel<?, ?>> value) {
        this.value = value;
    }

    public RuntimeValue<ZooModel<?, ?>> getValue() {
        return value;
    }

}
