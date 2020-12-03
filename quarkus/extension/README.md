# DJL Quarkus Extension

This is an extension for [quarkus](https://quarkus.io/) to better integrate DJL into quarkus applications.

Currently, this extension covers inference by letting you inject a Predictor with a model defined using the Quarkus configuration. You can access the Predictor by using:

```java
@Inject
DjlPredictorProducer predictorProducer;
```

To configure what model this predictor contains, the following configuration is available:

- quarkus.djl-model.input-class - **Required** Java class path String
- quarkus.djl-model.output-class - **Required** Java class path String
- quarkus.djl-model.application - Application String
- quarkus.djl-model.engine - Engine name (String)
- quarkus.djl-model.groupId - Model group Id (String)
- quarkus.djl-model.artifactId - Model artifact Id (String)
- quarkus.djl-model.filters.{filterName} - Value for filter {filterName} (String)
- quarkus.djl-model.modelName - Model name (String)

For more information, see the Javadoc for the extension's `DjlModelConfiguration` class and DJL's `Criteria` class.

To see this in action, view the [extension example](../extension-example/README.md).
