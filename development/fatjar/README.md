# How to distribute DJL application

This is an example of how to create distribution package for DJL application.

In this example you will learn how to package your application that can avoid download
native libraries at runtime in case you don't have internet access in production environment.

## Gradle

Run/test application with gradle

```
cd development/fatjar

./gradlew run
```

Distribute with `.zip` or `.tar` file

```shell
./gradlew assembleDist
```

You will find both `.zip` and `.tar` file in `build/distributions` folder.

Create fatjar:

```shell
./gradlew shadowJar

java -jar build/libs/fatjar-1.0-SNAPSHOT-all.jar
```

## Maven

Run/test application with maven

```
cd development/fatjar

mvn package
mvn exec:java
```

Create fatjar:

```shell
mvn package

java -jar target/fatjar-1.0-SNAPSHOT.jar
```

## SBT

Run/test application with sbt

```
cd development/fatjar

sbt run
```

Create fatjar:

```shell
sbt assembly

java -jar target/fatjar-assembly-1.0-SNAPSHOT.jar
```

## Testing offline mode

To ensure the application doesn't download the native dependencies at runtime, you can set system
property: `offline=true`.

You can test offline mode with gradle or using java command line option:

```shell
cd development/fatjar

# for gradle
./gradlew run -Doffline=true

# for maven
mvn package
mvn exec:java -Doffline=true
```

If your os doesn't match your native library, you will see the follow error:

```
Loading:     100% |████████████████████████████████████████|
Exception in thread "main" ai.djl.engine.EngineException: Failed to load PyTorch native library
	at ai.djl.pytorch.engine.PtEngine.newInstance(PtEngine.java:82)
	at ai.djl.pytorch.engine.PtEngineProvider.getEngine(PtEngineProvider.java:40)
	at ai.djl.engine.Engine.getEngine(Engine.java:181)
	at ai.djl.Model.newInstance(Model.java:99)
	at ai.djl.repository.zoo.BaseModelLoader.createModel(BaseModelLoader.java:189)
	at ai.djl.repository.zoo.BaseModelLoader.loadModel(BaseModelLoader.java:152)
	at ai.djl.repository.zoo.Criteria.loadModel(Criteria.java:168)
	at com.examples.FatJar.main(FatJar.java:41)
Caused by: java.lang.IllegalStateException: Failed to save pytorch index file
	at ai.djl.pytorch.jni.LibUtils.downloadPyTorch(LibUtils.java:399)
	at ai.djl.pytorch.jni.LibUtils.findNativeLibrary(LibUtils.java:282)
	at ai.djl.pytorch.jni.LibUtils.getLibTorch(LibUtils.java:87)
	at ai.djl.pytorch.jni.LibUtils.loadLibrary(LibUtils.java:75)
	at ai.djl.pytorch.engine.PtEngine.newInstance(PtEngine.java:53)
	... 7 more
Caused by: java.io.IOException: Offline model is enabled.
	at ai.djl.util.Utils.openUrl(Utils.java:457)
	at ai.djl.util.Utils.openUrl(Utils.java:443)
	at ai.djl.pytorch.jni.LibUtils.downloadPyTorch(LibUtils.java:394)
	... 11 more
```
