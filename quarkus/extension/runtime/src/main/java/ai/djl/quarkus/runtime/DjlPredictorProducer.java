package ai.djl.quarkus.runtime;

import ai.djl.inference.Predictor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/**
 * An injectable dependency that creates a {@link Predictor} using the model described in the
 * Quarkus Configuration.
 */
@ApplicationScoped
public class DjlPredictorProducer {

    private volatile Predictor<?, ?> predictor;

    void initialize(Predictor<?, ?> predictor) {
        this.predictor = predictor;
    }

    @Singleton
    @Produces
    public Predictor<?, ?> predictor() {
        return predictor;
    }
}
