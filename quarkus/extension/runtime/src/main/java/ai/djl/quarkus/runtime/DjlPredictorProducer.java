package ai.djl.quarkus.runtime;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * An injectable dependency that creates a {@link Predictor} using the model described in the
 * Quarkus Configuration.
 */
@ApplicationScoped
public class DjlPredictorProducer {

    private volatile ZooModel<?, ?> model;

    void initialize(ZooModel<?, ?> model) {
        this.model = model;
    }

    @Produces
    public Predictor<?, ?> predictor() {
        return model.newPredictor();
    }
}
