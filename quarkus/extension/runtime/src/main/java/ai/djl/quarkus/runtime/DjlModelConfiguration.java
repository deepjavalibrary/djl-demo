package ai.djl.quarkus.runtime;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Quarkus Configuration to describe a {@link ai.djl.Model}.
 */
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public class DjlModelConfiguration {

    /**
     * The {@link ai.djl.inference.Predictor} input class.
     */
    @ConfigItem
    public String inputClass;

    /**
     * The {@link ai.djl.inference.Predictor} output class.
     */
    @ConfigItem
    public String outputClass;

    /**
     * The model {@link ai.djl.Application}.
     */
    @ConfigItem
    public Optional<String> application;

    /**
     * The {@link ai.djl.engine.Engine}.
     */
    @ConfigItem
    public Optional<String> engine;

    /**
     * The model {@link ai.djl.repository.MRL} group id.
     */
    @ConfigItem
    public Optional<String> groupId;

    /**
     * The model {@link ai.djl.repository.MRL} group id.
     */
    @ConfigItem
    public Optional<String> artifactId;

    /**
     * The model property filters.
     *
     * @see ai.djl.repository.zoo.Criteria
     */
    @ConfigItem
    public Map<String, String> filters;

    /**
     * The model name.
     *
     * @see ai.djl.repository.zoo.Criteria
     */
    @ConfigItem
    public Optional<String> modelName;

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "DjlModelConfiguration{" +
                "inputClass='" + inputClass + '\'' +
                ", outputClass='" + outputClass + '\'' +
                ", application=" + application +
                ", engine=" + engine +
                ", groupId=" + groupId +
                ", artifactId=" + artifactId +
                ", filters=" + filters +
                ", modelName=" + modelName +
                '}';
    }
}
